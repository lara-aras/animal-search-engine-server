/*
 * Filename: Server.java
 * Author: Lara Aras
 * Created: 05/10/2021
 * Operating System: Windows 10 Enterprise
 * Version: Project 1
 * Description: This file contains the functionality for connecting to 
 *              and interacting with the database to handle requests from the
 *              client.
 */
package animalsearchengineserver;

import java.sql.*;
import java.util.*;

/**
 * Class used to connect to and interact with the database to handle client
 * requests.
 *
 * @author Lara Aras
 */
public class HandleClientRequest {

    private final String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private final String connURL = "jdbc:sqlserver://localhost:1433;databaseName=AnimalSearchEngine;user=admin;password=1234;";
    private Connection conn;

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Constructor for the class. Creates connection to database.
     *
     * @author Lara Aras
     */
    public HandleClientRequest() {
        connectToDatabase();
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Determines which method to call based on the action specified in the
     * input sent from the client, and returns response to be sent back to the
     * client.
     *
     * @author Lara Aras
     * @param input
     * @return response
     */
    public String handleRequest(HashMap input) {
        String response = "";

        try {
            /* The first key-value pair of the input map contains the keyword 
               for the method to be executed */
            switch ((String) input.get("action")) {
                case "login":
                    response = checkLogin(input);
                    break;
                case "search":
                    response = getSearchResults(input);
                    break;
                case "insert":
                    response = insertRecord(input);
                    break;
                case "update":
                    response = updateRecord(input);
                    break;
                case "delete":
                    response = deleteRecord(input);
                    break;
                case "getanimals":
                    response = getAllAnimals();
                    break;
                case "getspecies":
                    response = getAllSpecies();
                    break;
                default:
                    response = "invalid";
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return response;
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Makes connection to database
     *
     * @author Lara Aras
     */
    private void connectToDatabase() {
        try {
            /* Instantiate class for SQL Server driver */
            Class.forName(driverName);

            /* Assign Connection object to static conn variable */
            conn = DriverManager.getConnection(connURL);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Queries the database to find the login details entered by the user.
     *
     * @author Lara Aras
     * @param input
     * @return response
     * @throws SQLException
     */
    private String checkLogin(HashMap input) throws SQLException {
        /* Create prepared statement for query */
        String query = "SELECT userID FROM admin "
                + "WHERE username = ? "
                + "AND password = ?";
        PreparedStatement searchQuery = conn.prepareStatement(query);
        searchQuery.setString(1, (String) input.get("username"));
        searchQuery.setString(2, (String) input.get("password"));

        /* Execute query and create a string for each row returned, and add 
           strings to the resultList to be returned to the client */
        ResultSet result = searchQuery.executeQuery();
        String response = result.next() ? "success" : "failure";

        return response;
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Queries the database to find rows that match the search query.
     *
     * @author Lara Aras
     * @param input
     * @return response
     * @throws SQLException
     */
    private String getSearchResults(HashMap input) throws SQLException {
        /* Create prepared statement for query */
        String query = "SELECT animalName, description, speciesName FROM animal"
                + " JOIN species ON animal.speciesID = species.speciesID "
                + "WHERE animalName LIKE ? "
                + "OR description like ? "
                + "OR speciesName like ?";
        PreparedStatement searchQuery = conn.prepareStatement(query);

        /* Add wildcard symbols to prepared statement parameters for "LIKE"
           keywords in query */
        searchQuery.setString(1, "%" + (String) input.get("searchString") + "%");
        searchQuery.setString(2, "%" + (String) input.get("searchString") + "%");
        searchQuery.setString(3, "%" + (String) input.get("searchString") + "%");

        ResultSet result = searchQuery.executeQuery();
        String responseString = "";

        /* Find out whether the cursor is before the first row in the ResultSet 
           object to determine if there are any rows */
        if (!result.isBeforeFirst()) {
            /* If no results were found */
            responseString = "no results";
        } else {
            /* Use StringBuilder class to add each row to the response string in 
           the while loop */
            StringBuilder response = new StringBuilder();
            while (result.next()) {
                /* Use ":" character as row delimeter, and "@" as column 
                   delimeter for the response string to be split up and 
                   displayed as a table in the GUI */
                String animalString = result.getString(1)
                        + "@" + result.getString(2)
                        + "@" + result.getString(3) + ":";

                response.append(animalString);
            }
            responseString = response.toString();
        }

        return responseString;
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Inserts new row into the database using the details in the request sent
     * by the client.
     *
     * @author Lara Aras
     * @param input
     * @return response
     * @throws SQLException
     */
    private String insertRecord(HashMap input) throws SQLException {
        PreparedStatement insertQuery;
        int rowCount = 0;

        if (input.get("tableName").equals("animal")) {
            String query
                    = "INSERT INTO animal(animalName, description, speciesID) "
                    + "VALUES(?, ?, ?)";

            insertQuery = conn.prepareStatement(query);
            insertQuery.setString(1, (String) input.get("animalName"));
            insertQuery.setString(2, (String) input.get("description"));
            insertQuery.setInt(3, (int) input.get("speciesID"));

            rowCount = insertQuery.executeUpdate();
        } else if (input.get("tableName").equals("species")) {
            String query
                    = "INSERT INTO species(speciesName) "
                    + "VALUES(?)";

            insertQuery = conn.prepareStatement(query);
            insertQuery.setString(1, (String) input.get("speciesName"));

            rowCount = insertQuery.executeUpdate();
        }

        /* rowCount will be 1 on update success, set response string according
           to this value */
        String response = rowCount == 1 ? "success" : "failure";
        return response;
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Updates a row in the database using the details in the request sent by
     * the client.
     *
     * @author Lara Aras
     * @param input
     * @return response
     * @throws SQLException
     */
    private String updateRecord(HashMap input) throws SQLException {
        PreparedStatement updateQuery;
        int rowCount = 0;

        if (input.get("tableName").equals("animal")) {
            String query
                    = "UPDATE animal "
                    + "SET animalName = ?, description = ?, speciesID = ? "
                    + "WHERE animalID = ?";

            updateQuery = conn.prepareStatement(query);
            updateQuery.setString(1, (String) input.get("animalName"));
            updateQuery.setString(2, (String) input.get("description"));
            updateQuery.setInt(3, (int) input.get("speciesID"));
            updateQuery.setInt(4, (int) input.get("animalID"));

            rowCount = updateQuery.executeUpdate();
        } else if (input.get("tableName").equals("species")) {
            String query
                    = "UPDATE species "
                    + "SET speciesName = ? "
                    + "WHERE speciesID = ?";

            updateQuery = conn.prepareStatement(query);
            updateQuery.setString(1, (String) input.get("speciesName"));
            updateQuery.setInt(2, (int) input.get("speciesID"));

            rowCount = updateQuery.executeUpdate();
        }

        /* rowCount will be 1 on update success, set response string according
           to this value */
        String response = rowCount == 1 ? "success" : "failure";
        return response;
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 05/10/2021
     * <p>
     * Deletes a row in the database using the details in the request sent by
     * the client.
     *
     * @author Lara Aras
     * @param input
     * @return response
     * @throws SQLException
     */
    private String deleteRecord(HashMap input) throws SQLException {
        PreparedStatement deleteQuery;
        int rowCount = 0;

        if (input.get("tableName").equals("animal")) {
            String query
                    = "DELETE FROM animal "
                    + "WHERE animalID = ?";

            deleteQuery = conn.prepareStatement(query);
            deleteQuery.setInt(1, (int) input.get("animalID"));

            rowCount = deleteQuery.executeUpdate();
        } else if (input.get("tableName").equals("species")) {
            /* Deletion from species table not allowed, return immediately */
            return "not allowed";
        }

        /* rowCount will be 1 on update success, set response string according
           to this value */
        String response = rowCount == 1 ? "success" : "failure";
        return response;
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 07/10/2021
     * <p>
     * Get all animals in the database to populate form fields when updating and
     * deleting animals in the database admin GUI.
     *
     * @author Lara Aras
     * @return responseString
     */
    private String getAllAnimals() throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM animal");
        String responseString = "";

        /* Find out whether the cursor is before the first row in the ResultSet 
           object to determine if there are any rows */
        if (!result.isBeforeFirst()) {
            /* If no results were found */
            responseString = "no results";
        } else {
            /* Use StringBuilder class to add each row to the response string in 
           the while loop */
            StringBuilder response = new StringBuilder();
            while (result.next()) {
                /* Use ":" character as row delimeter, and "@" as column 
                   delimeter for the response string to be split up and 
                   saved in an array */
                String animalString = (String.valueOf(result.getInt(1)))
                        + "@" + result.getString(2)
                        + "@" + result.getString(3)
                        + "@" + (String.valueOf(result.getInt(4))) + ":";

                response.append(animalString);
            }
            responseString = response.toString();
        }

        return responseString;
    }

    /**
     * Version: Project 1
     * <p>
     * Date: 07/10/2021
     * <p>
     * Get all species in the database to populate form fields when updating
     * species and creating new animals in the database admin GUI.
     *
     * @author Lara Aras
     * @return responseString
     */
    private String getAllSpecies() throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM species");
        String responseString = "";

        /* Find out whether the cursor is before the first row in the ResultSet 
           object to determine if there are any rows */
        if (!result.isBeforeFirst()) {
            /* If no results were found */
            responseString = "no results";
        } else {
            /* Use StringBuilder class to add each row to the response string in 
           the while loop */
            StringBuilder response = new StringBuilder();
            while (result.next()) {
                /* Use ":" character as row delimeter, and "@" as column 
                   delimeter for the response string to be split up and 
                   saved in an array */
                String animalString = (String.valueOf(result.getInt(1)))
                        + "@" + result.getString(2) + ":";

                response.append(animalString);
            }
            responseString = response.toString();
        }

        return responseString;
    }
}
