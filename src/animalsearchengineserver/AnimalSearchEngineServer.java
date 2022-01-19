/*
 * Filename: AnimalSearchEngine.java
 * Author: Lara Aras
 * Created: 04/10/2021
 * Operating System: Windows 10 Enterprise
 * Version: Project 1
 * Description: This file contains the main class of the client application.
 */
package animalsearchengineserver;

import javax.swing.*;

/**
 * The main class of the application, serves as the entry point for the rest of
 * the server application.
 *
 * @author Lara Aras
 */
public class AnimalSearchEngineServer {

    /**
     * Version: Project 1
     * <p>
     * Date: 07/10/2021
     * <p>
     * Description: The main method of the AnimalSearchEngineServer class.
     *
     * @author Lara Aras
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        /* Set look and feel for GUI */
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
        }

        /* Instantiate main frame class and start server */
        Server server = new Server();
        server.setVisible(true);
        server.initServer();
    }
}
