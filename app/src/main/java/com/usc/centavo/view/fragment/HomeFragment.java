package com.usc.centavo.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.databinding.FragmentHomeBinding;
import com.usc.centavo.model.Transaction;
import com.usc.centavo.view.adapter.TransactionAdapter;
import com.usc.centavo.viewmodel.TransactionViewModel;
import com.usc.centavo.viewmodel.CategoryViewModel;
import com.usc.centavo.model.Category;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import com.usc.centavo.viewmodel.BudgetViewModel;
import com.usc.centavo.model.Budget;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.ArrayAdapter;
import com.usc.centavo.R;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.usc.centavo.view.adapter.GoalSummaryAdapter;
import com.usc.centavo.viewmodel.GoalViewModel;

public class HomeFragment extends Fragment {
    private static final int MAX_RECENT_TRANSACTIONS = 3;

    private FragmentHomeBinding binding;
    private TransactionViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private GoalViewModel goalViewModel;
    private TransactionAdapter adapter;
    private GoalSummaryAdapter goalSummaryAdapter;
    private List<Category> categoryList = new ArrayList<>();
    private LineChart lineChart;
    private MaterialAutoCompleteTextView dropdownTrendDateRange;
    private MaterialAutoCompleteTextView dropdownTrendCategory;
    private Map<String, String> categoryNameToIdMap = new HashMap<>();
    private Map<String, String> categoryColorMap = new HashMap<>();
    private String lastChartCategoryId = null;
    private Date lastChartStartDate = null;
    private Date lastChartEndDate = null;
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Category> allCategories = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        // Initialize views from binding
        lineChart = binding.lineChartTrend;
        dropdownTrendDateRange = binding.dropdownTrendDateRange;
        dropdownTrendCategory = binding.dropdownTrendCategory;

        setupRecyclerView();
        setupGoalSummaryRecyclerView();
        setupChart();
        setupDropdowns();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new ArrayList<>());
        binding.recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRecent.setAdapter(adapter);
    }

    private void setupGoalSummaryRecyclerView() {
        goalSummaryAdapter = new GoalSummaryAdapter();
        binding.recyclerViewGoalsSummary.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewGoalsSummary.setAdapter(goalSummaryAdapter);
    }

    private void setupDropdowns() {
        // Date range dropdown
        List<String> dateRanges = new ArrayList<>();
        dateRanges.add("Last 7 days");
        dateRanges.add("Last 30 days");
        dateRanges.add("This month");
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, dateRanges);
        dropdownTrendDateRange.setAdapter(dateAdapter);
        dropdownTrendDateRange.setThreshold(0);
        dropdownTrendDateRange.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) dropdownTrendDateRange.showDropDown(); });
        dropdownTrendDateRange.setText(dateRanges.get(0), false);
        dropdownTrendDateRange.setOnItemClickListener((parent, view, position, id) -> updateTrendChartQuery());
        dropdownTrendCategory.setOnItemClickListener((parent, view, position, id) -> updateTrendChartQuery());
    }

    private void setupChart() {
        // Basic chart setup
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getLegend().setEnabled(false);
    }

    private void setupObservers() {
        // Observe categories
        categoryViewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            allCategories = categories != null ? categories : new ArrayList<>();
            List<String> categoryNames = new ArrayList<>();
            categoryNameToIdMap.clear();
            categoryColorMap.clear();
            for (Category c : allCategories) {
                categoryNames.add(c.getName());
                categoryNameToIdMap.put(c.getName(), c.getCategoryId());
                categoryColorMap.put(c.getCategoryId(), c.getColor());
            }
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
            dropdownTrendCategory.setAdapter(catAdapter);
            dropdownTrendCategory.setThreshold(0);
            if (!categoryNames.isEmpty()) dropdownTrendCategory.setText(categoryNames.get(0), false);
            // Set default chart if not set
            if (lastChartCategoryId == null && !categoryNames.isEmpty()) {
                dropdownTrendDateRange.setText("Last 7 days", false);
                dropdownTrendCategory.setText(categoryNames.get(0), false);
                updateTrendChartQuery();
            }
            // Update category color map for recent transactions
            if (adapter != null) {
                adapter.setCategoryColorMap(categoryColorMap);
            }
        });

        // Observe transactions
        viewModel.getTransactionsLiveData().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions != null ? transactions : new ArrayList<>();
            // Update recent transactions
            List<Transaction> limitedTransactions = allTransactions.size() > MAX_RECENT_TRANSACTIONS
                    ? allTransactions.subList(0, MAX_RECENT_TRANSACTIONS)
                    : allTransactions;
            if (adapter != null) {
                adapter.setTransactions(limitedTransactions);
            }
            // Update chart if filters are set
            if (lastChartCategoryId != null && lastChartStartDate != null && lastChartEndDate != null) {
                updateTrendChart();
            }
        });
        
        goalViewModel.getGoalsLiveData().observe(getViewLifecycleOwner(), goals -> {
            if (goalSummaryAdapter != null) {
                goalSummaryAdapter.submitList(goals);
            }
        });

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.onErrorHandled();
            }
        });

        loadTransactions();
    }

    private void updateTrendChartQuery() {
        if (dropdownTrendCategory == null || dropdownTrendDateRange == null) return;
        String selectedCategory = dropdownTrendCategory.getText().toString();
        String categoryId = categoryNameToIdMap.get(selectedCategory);
        if (categoryId == null) return;
        String selectedRange = dropdownTrendDateRange.getText().toString();
        Calendar cal = Calendar.getInstance();
        // Set endDate to end of today
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();
        Date startDate;
        switch (selectedRange) {
            case "Last 7 days":
                cal.add(Calendar.DAY_OF_YEAR, -6);
                startDate = cal.getTime();
                break;
            case "Last 30 days":
                cal.add(Calendar.DAY_OF_YEAR, -29);
                startDate = cal.getTime();
                break;
            case "This month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
                break;
            default:
                cal.add(Calendar.DAY_OF_YEAR, -6);
                startDate = cal.getTime();
        }
        lastChartCategoryId = categoryId;
        lastChartStartDate = startDate;
        lastChartEndDate = endDate;
        updateTrendChart();
    }

    private void updateTrendChart() {
        if (lastChartCategoryId == null || lastChartStartDate == null || lastChartEndDate == null || lineChart == null) return;
        // Filter and aggregate transactions
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction tx : allTransactions) {
            if (!lastChartCategoryId.equals(tx.getCategoryId())) continue;
            if (!"Expense".equalsIgnoreCase(tx.getType())) continue;
            Date txDate = tx.getTransactionDate();
            if (txDate == null) continue;
            if (!txDate.before(lastChartStartDate) && !txDate.after(lastChartEndDate)) {
                filtered.add(tx);
            }
        }
        // Group by local date
        java.text.SimpleDateFormat keyFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
        keyFormat.setTimeZone(java.util.TimeZone.getDefault());
        java.text.SimpleDateFormat labelFormat = new java.text.SimpleDateFormat("M/d", Locale.US);
        labelFormat.setTimeZone(java.util.TimeZone.getDefault());
        Map<String, Float> daySums = new java.util.LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        Calendar iter = Calendar.getInstance();
        iter.setTime(lastChartStartDate);
        while (!iter.getTime().after(lastChartEndDate)) {
            String key = keyFormat.format(iter.getTime());
            String label = labelFormat.format(iter.getTime());
            daySums.put(key, 0f);
            labels.add(label);
            iter.add(Calendar.DAY_OF_YEAR, 1);
        }
        for (Transaction tx : filtered) {
            String key = keyFormat.format(tx.getTransactionDate());
            if (daySums.containsKey(key)) {
                daySums.put(key, daySums.get(key) + (float) tx.getAmount());
            }
        }
        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (String key : daySums.keySet()) {
            entries.add(new Entry(i++, daySums.get(key)));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Spending");
        dataSet.setColor(getResources().getColor(R.color.md_theme_primary, null));
        dataSet.setCircleColor(getResources().getColor(R.color.md_theme_primary, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(Math.min(labels.size(), 7), false); // Show up to 7 labels, let chart decide
        xAxis.setLabelRotationAngle(-45f); // Tilt labels for clarity
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();
    }

    private void loadTransactions() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            viewModel.getTransactionsForUser(userId);
            categoryViewModel.loadCategories();
            goalViewModel.getGoalsForUser(userId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}