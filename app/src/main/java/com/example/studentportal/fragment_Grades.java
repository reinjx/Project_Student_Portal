package com.example.studentportal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import android.widget.TableLayout;
import android.widget.TableRow;


public class fragment_Grades extends Fragment {

    private TextView etName;
    private TextView studentNumber;
    private ImageView profileImageView;
    private String currentStudentNumber; // Store current student number

    private TableLayout tableLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment__grades, container, false);

        // Initialize views
        etName = rootView.findViewById(R.id.et_Name);
        studentNumber = rootView.findViewById(R.id.studentnumber);
        profileImageView = rootView.findViewById(R.id.profileimage);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        // Get student number from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        if (currentStudentNumber != null) {
            studentNumber.setText(currentStudentNumber); // Set the student number in the TextView
            loadUserName(currentStudentNumber); // Load the name from database
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void loadUserName(String studentNumber) {
        new Thread(() -> {
            try {
                // Get the connection using ConnectionClass
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                // Query the enrollstudentinformation table
                String query = "SELECT firstName, lastName FROM enrollstudentinformation WHERE studentNumber = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, studentNumber);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String firstName = resultSet.getString("firstName");
                    String lastName = resultSet.getString("lastName");

                    // Update the name on the main thread (UI thread)
                    getActivity().runOnUiThread(() -> etName.setText(firstName + " " + lastName));
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show());
                }

                resultSet.close();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void addRowToTable(String subjectId, Double gradeValue) {
        // Create a new table row
        TableRow row = new TableRow(getContext());
        // Set layout parameters for the row
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layoutParams);
        // Create and set text views for subject and grade
        TextView subjectTextView = new TextView(getContext());
        subjectTextView.setText(subjectId);
        subjectTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium)); // Apply PoppinsMedium
        subjectTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        subjectTextView.setPadding(150, 16, 16, 16);
        subjectTextView.setTextColor(getResources().getColor(R.color.black));
        subjectTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START); // Align left
        TextView gradeTextView = new TextView(getContext());
        gradeTextView.setText(String.valueOf(gradeValue));
        gradeTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium)); // Apply PoppinsMedium
        gradeTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        gradeTextView.setPadding(16, 16, 170, 16);
        gradeTextView.setTextColor(getResources().getColor(R.color.black));
        gradeTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END); // Align right
        // Add views to the row
        row.addView(subjectTextView);
        row.addView(gradeTextView);
        // Add row to the table layout
        tableLayout.addView(row);

    }



}
