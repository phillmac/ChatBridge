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
                 

	public static void main(String[] args) {
            //new ChatBotExample();
            //new TS3Bot();
            try {
                System.out.println("Chatbridge initalizing");
                                //Configure what we want our bot to do
                
                 InputStream ircConfigInput = null;
                try {
                    ircConfigInput =  new FileInputStream(new File("ircconfig.yml"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("Could not find ircconfig.yml. Please check that it exists.");
                }

                Yaml ircConfigParser = new Yaml();
                Map<String,String> ircConfigMap = (Map<String,String>)ircConfigParser.load(ircConfigInput);
                
                Configuration configuration = new Configuration.Builder()
                        .setName(ircConfigMap.get("nick")) //Set the nick of the bot.
                        .setServerHostname(ircConfigMap.get("host")) //
                        .addAutoJoinChannel(ircConfigMap.get("channel"), "") //
                        .addListener(new IRCListener()) //Add our listener that will be called on Events
                        .buildConfiguration();

               //Create our bot with the configuration
                MultiBotManager<PircBotX> manager = new MultiBotManager();
                manager.addBot(configuration);
                //PircBotX bot = new PircBotX(configuration);
                //Connect to the server
                //bot.startBot();
                manager.start();
                ircbotmanager = manager;
                
               ts3 =  new TS3Bot();
                
                
            } catch (Exception ex) {
                Logger.getLogger(irctots3chat.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
	}
        public static TS3Bot getTS3(){
            return ts3;
            
        
        }
        
        public static MultiBotManager<PircBotX> getIRCManger () {
        return ircbotmanager;
               

}
        public static String executeCommand(String[] command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
        
}
 