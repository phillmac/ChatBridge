/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.phillm.irctots3chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Phillip
 */
public class irctots3chat {

    private static TS3Bot ts3;
    private static MultiBotManager<PircBotX> ircbotmanager;
    public static Map<String, String> ircConfigMap = null;

    public static void main(String[] args) {
        connectIRC();
        connectTS3();

    }

    /**
     * Loads configuration from irconfig.yml and attempts to
     * connect to the specified server.
     */
    public static void connectIRC() {
        try {
            System.out.println("Chatbridge initalizing");
            //Configure what we want our bot to do

            InputStream ircConfigInput = null;
            try {
                ircConfigInput = new FileInputStream(new File("ircconfig.yml"));
            } catch (FileNotFoundException e) {
                System.out.println("Could not find ircconfig.yml");
                System.out.println("Extracting ircconfig.yml");
                extractConfig("ircconfig.ym", "ircconfig.yml");
                System.exit(0);
            }

            Yaml ircConfigParser = new Yaml();
            ircConfigMap = (Map<String, String>) ircConfigParser.load(ircConfigInput);

            Configuration configuration = new Configuration.Builder()
                    .setName(ircConfigMap.get("nick")) //Set the nick of the bot.
                    .setServerHostname(ircConfigMap.get("host")) //
                    .addAutoJoinChannel(ircConfigMap.get("channel"), "") //
                    .addListener(new IRCListener()) //Add our listener that will be called on Events
                    .buildConfiguration();

            //Create our bot with the configuration
            MultiBotManager<PircBotX> manager = new MultiBotManager();
            manager.addBot(configuration);
            manager.start();
            ircbotmanager = manager;

        } catch (Exception ex) {
            Logger.getLogger(irctots3chat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     */
    public static void connectTS3() {
        try {
            ts3 = new TS3Bot();
        } catch (Exception ex) {
            Logger.getLogger(irctots3chat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @return
     */
    public static TS3Bot getTS3() {
        return ts3;

    }

    public static MultiBotManager<PircBotX> getIRCManger() {
        return ircbotmanager;

    }

    /**
     *Run a shell command and return the resulting text output.
     * @param command Shell command to run, first array element is the command name, following
     * elements are the parameters.
     * @return The resulting text output of the command, or the string "Error" on failure.
     */
    public static String executeCommand(String[] command) {

        StringBuilder output = new StringBuilder();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error while executing command");
            return "Error";
           
        }

        return output.toString();

    }

    /**
     *Extract the specified configuration file.
     * @param internalPath Internal file name.
     * @param fileName external file name to extract to.
     */
    public static void extractConfig(String internalPath, String fileName) {
        FileOutputStream output = null;
        int bytesRead = 0;
        try {
            output = new FileOutputStream(fileName);
            try (InputStream input = irctots3chat.class.getClassLoader().getResourceAsStream(internalPath)) {
                byte[] buffer = new byte[4096];
                try {
                    bytesRead = input.read(buffer);
                } catch (IOException ex) {
                    Logger.getLogger(irctots3chat.class.getName()).log(Level.SEVERE, null, ex);
                }
                while (bytesRead != -1) {
                    output.write(buffer, 0, bytesRead);
                    bytesRead = input.read(buffer);
                }
                output.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(irctots3chat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(irctots3chat.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(irctots3chat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
