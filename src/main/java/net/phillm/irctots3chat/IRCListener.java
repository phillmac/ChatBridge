/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.phillm.irctots3chat;





import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;




/**
 *
 * @author Phillip
 */

    
    


public class IRCListener extends ListenerAdapter {
        @Override
        public void onGenericMessage(GenericMessageEvent event) {
                //When someone says hello, respond with Hello World
                if (event.getMessage().startsWith(".ping")){
                        event.respond("Pong!");
                } else {
                    
                    irctots3chat.getTS3().getAPI().sendChannelMessage(event.getUser().getNick() + " : " + event.getMessage());
                }
                }
        }
        


