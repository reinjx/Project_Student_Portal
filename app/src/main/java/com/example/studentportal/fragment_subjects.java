package com.example.studentportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class fragment_subjects extends Fragment {

    TextView studentnumber, nameheader;
    CircularImageView profileImage;
    AutoCompleteTextView dropdown;
    TableLayout tableLayout;
    private String currentStudentNumber;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_subjects, container, false);

        // Initialize UI components

        dropdown = rootView.findViewById(R.id.role);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        if (currentStudentNumber != null) {
            studentnumber.setText(currentStudentNumber);
            loadUserName(currentStudentNumber);
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        // Populate dropdown and set selection listener
        setupDropdown();

        return rootView;
    }

    private void setupDropdown() {
        // Updated options with "First Sem" and "Second Sem"
        String[] schoolYearTermOptions = {
                "2023 - First Sem",
                "2023 - Second Sem",
                "2024 - First Sem",
                "2024 - Second Sem"
        };

        // ArrayAdapter to populate the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                schoolYearTermOptions
        );

        dropdown.setAdapter(adapter);

        // Handle dropdown selection
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = schoolYearTermOptions[position];
            String[] parts = selected.split(" - ");
            String year = parts[0];   // Extracts the year (e.g., "2023")
            String semester = parts[1];  // Extracts the semester (e.g., "First Sem" or "Second Sem")

            // Determine the term value based on the semester
            String term = (semester.equals("First Sem")) ? "1" : "2";  // "First Sem" = 1, "Second Sem" = 2

            // Load subjects for the selected school year and semester (term)
            loadSubjects(year, term);
        });
    }

    private void loadUserName(String currentStudentNumber) {
        new Thread(() -> {
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                String query = "SELECT firstName, lastName FROM enrollstudentinformation WHERE studentNumber = ?";
                Log.e("SQL Query", "Executing query: " + query + " with studentNumber = " + currentStudentNumber);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, currentStudentNumber);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String firstName = resultSet.getString("firstName");
                    String lastName = resultSet.getString("lastName");
                    Log.e("UserData", "User found: " + firstName + " " + lastName);

                    getActivity().runOnUiThread(() -> nameheader.setText(firstName + " " + lastName));
                } else {
                    getActivity().runOnUiThread(() -> {
                        Log.e("UserData", "User not found");
                        Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                    });
                }

                resultSet.close();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                getActivity().runOnUiThread(() -> {
                    Log.e("Database Error", "Error: " + e.getMessage());
                    Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadSubjects(String year, String term) {
        // Clear previous rows (except the header row)
        getActivity().runOnUiThread(() -> {
            int childCount = tableLayout.getChildCount();
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1); // Remove all rows except header
                Log.e("Table", "Cleared previous rows");
            }
        });

        new Thread(() -> {
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                // Correct query to select subjectCode and subjectTitle
                String query = "SELECT subjectCode, subjectTitle FROM enrollsubjectstbl WHERE schoolYear = ? AND term = ?";
                Log.e("SQL Query", "Executing query: " + query + " with schoolYear = " + year + " and term = " + term);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, year);
                statement.setString(2, term);
                ResultSet resultSet = statement.executeQuery();

                // Log the result count
                int resultCount = 0;

                while (resultSet.next()) {
                    resultCount++;
                    String subjectCode = resultSet.getString("subjectCode");
                    String subjectTitle = resultSet.getString("subjectTitle");
                    Log.e("Subject Data", "Subject found: " + subjectCode + " - " + subjectTitle);

                    // Add row to the table on the main thread
                    getActivity().runOnUiThread(() -> addTableRow(subjectCode, subjectTitle));
                }

                // Log the number of results found
                Log.e("SQL Query", "Number of subjects found: " + resultCount);

                resultSet.close();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                getActivity().runOnUiThread(() -> {
                    Log.e("Database Error", "Error: " + e.getMessage());
                    Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void addTableRow(String subjectCode, String subjectTitle) {
        TableRow row = new TableRow(getActivity());

        // Set padding for the row
        row.setPadding(0, 10, 0, 10); // Increased vertical padding for better spacing
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create and set the Subject Code TextView
        TextView subjectCodeView = new TextView(getActivity());
        subjectCodeView.setText(subjectCode);
        subjectCodeView.setPadding(20, 20, 20, 20); // Add padding for better readability
        subjectCodeView.setGravity(Gravity.CENTER);  // Center text horizontally
        subjectCodeView.setTextSize(16);  // Set text size for readability
        subjectCodeView.setBackgroundColor(getResources().getColor(android.R.color.white));  // Set background color to white
        subjectCodeView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));  // Equal space distribution

        // Create and set the Subject Title TextView
        TextView subjectTitleView = new TextView(getActivity());
        subjectTitleView.setText(subjectTitle);
        subjectTitleView.setPadding(20, 20, 20, 20); // Add padding for better readability
        subjectTitleView.setGravity(Gravity.CENTER);  // Center text horizontally
        subjectTitleView.setTextSize(16);  // Set text size for readability
        subjectTitleView.setBackgroundColor(getResources().getColor(android.R.color.white));  // Set background color to white
        subjectTitleView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));  // Subject title column gets more space

        // Add both TextViews to the row
        row.addView(subjectCodeView);
        row.addView(subjectTitleView);

        // Add the row to the TableLayout
        getActivity().runOnUiThread(() -> {
            tableLayout.addView(row);
            Log.e("Table", "Row added: " + subjectCode + " - " + subjectTitle);

            // Log the number of rows after adding
            Log.e("Table", "Total rows: " + tableLayout.getChildCount());
        });
    }


}
