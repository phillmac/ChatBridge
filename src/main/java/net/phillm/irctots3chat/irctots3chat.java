/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.phillm.irctots3chat;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;

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
                                //Configure what we want our bot to do
//                Configuration configuration = new Configuration.Builder()
//                                .setName("TS3") //Set the nick of the bot. CHANGE IN YOUR CODE
//                                .setServerHostname("64.182.125.140") //Join the freenode network
//                                .addAutoJoinChannel("#mcserverchat-test", "mcircchat112") //Join the official #pircbotx channel
//                                .addListener(new IRCListener()) //Add our listener that will be called on Events
//                                .buildConfiguration();
//
//                //Create our bot with the configuration
//                MultiBotManager<PircBotX> manager = new MultiBotManager();
//                manager.addBot(configuration);
                //PircBotX bot = new PircBotX(configuration);
                //Connect to the server
                //bot.startBot();
//                manager.start();
//                ircbotmanager = manager;
                
//               ts3 =  new TS3Bot();
                
                
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
        
}
