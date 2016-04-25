/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.phillm.chatbridge;

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
import org.pircbotx.Configuration.Builder;
import org.pircbotx.MultiBotManager;
import org.yaml.snakeyaml.Yaml;
import ro.fortsoft.pf4j.PluginWrapper;
import net.phillm.jc2.JC2Plugin;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author Phillip
 */
@Extension
public class ChatBridge extends JC2Plugin {

    private static TS3Bot ts3;
    private static MultiBotManager ircbotmanager;
    public static Map<String, String> ircConfigMap = null;
    private static final String version = "1.0.4.2";

    @Override
    public void start() {
        System.out.println("Chatbridge version " + version + " initalizing");

        connectIRC();
        connectTS3();
        
    }

    /**
     * Loads configuration from irconfig.yml and attempts to connect to the
     * specified server.
     */
    public static void connectIRC() {
        try {

            //Configure what we want our bot to do
            InputStream ircConfigInput = null;
            try {
                ircConfigInput = new FileInputStream(new File("ircconfig.yml"));
            } catch (FileNotFoundException e) {
                System.out.println("Could not find ircconfig.yml");
                System.out.println("Extracting ircconfig.yml");
                extractConfig("ircconfig.yml", "ircconfig.yml");
            }

            Yaml ircConfigParser = new Yaml();
            if (ircConfigInput != null) {
                ircConfigMap = (Map<String, String>) ircConfigParser.load(ircConfigInput);
                if (!ircConfigMap.get("host").equals("")) {
                    Builder configuration = new Builder();
                    configuration.addServer(ircConfigMap.get("host")); //
                    if (!ircConfigMap.get("nick").equals("")) {
                        configuration.setName(ircConfigMap.get("nick")); //Set the nick of the bot.
                    }
                    if (!ircConfigMap.get("channel").equals("")) {
                        configuration.addAutoJoinChannel(ircConfigMap.get("channel")); //
                    }
                    if (!ircConfigMap.get("autoreconnect").equals("")) {
                        configuration.setAutoReconnect(ircConfigMap.get("autoreconnect").equals("true"));
                    }
                    configuration.addListener(new IRCListener()); //Add our listener that will be called on Events

                    //Create our bot with the configuration
                    ircbotmanager = new MultiBotManager();
                    ircbotmanager.addBot(configuration.buildConfiguration());
                    ircbotmanager.start();
                } else {
                    System.out.println("Check that the host is set correctly in ircconfig.yml");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(ChatBridge.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Read ts3config.yml and attempt to create a connection to the specified
     * server
     */
    public static void connectTS3() {
        try {
            ts3 = new TS3Bot();
        } catch (Exception ex) {
            Logger.getLogger(ChatBridge.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @return Application's TS3Bot
     */
    public static TS3Bot getTS3() {
        return ts3;

    }

    /**
     * Gets a reference to the application's static MultiBotManager
     *
     * @return Application's MultiBotManager
     */
    public static MultiBotManager getIRCManger() {
        return ircbotmanager;

    }

    /**
     * Run a shell command and return the resulting text output.
     *
     * @param command Shell command to run, first array element is the command
     * name, following elements are the parameters.
     * @return The resulting text output of the command, or the string "Error"
     * on failure.
     */
    public static String executeShellCommand(String[] command) {

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
     * Extract the specified configuration file.
     *
     * @param internalPath Internal file name.
     * @param fileName external file name to extract to.
     */
    public static void extractConfig(String internalPath, String fileName) {
        FileOutputStream output = null;
        int bytesRead = 0;
        try {
            output = new FileOutputStream(fileName);
            try (InputStream input = ChatBridge.class.getClassLoader().getResourceAsStream(internalPath)) {
                byte[] buffer = new byte[4096];
                try {
                    bytesRead = input.read(buffer);
                } catch (IOException ex) {
                    Logger.getLogger(ChatBridge.class.getName()).log(Level.SEVERE, null, ex);
                }
                while (bytesRead != -1) {
                    output.write(buffer, 0, bytesRead);
                    bytesRead = input.read(buffer);
                }
                output.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChatBridge.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatBridge.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ChatBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ChatBridge(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getVersion() {
        return version;
    }

}
