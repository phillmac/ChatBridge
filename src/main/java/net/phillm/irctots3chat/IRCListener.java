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
                 System.out.println("IRC onGenericMessage fired");
                if (event.getMessage().startsWith(".ping")){
                        event.respond("Pong!");
                } else {
                    String nickname = event.getUser().getNick();
                    String message = event.getMessage();
                    irctots3chat.getTS3().getAPI().sendChannelMessage(nickname + " : " + message);
                    if (nickname.equalsIgnoreCase("skype")){
                        System.out.println("nick " + nickname + "is skype. not sending msg");
                    }else {
                        
                        irctots3chat.executeCommand(new String[]{"./skype-msg.sh", nickname + ": " + message});
                        
                    System.out.println("sent to skype: ./skype-msg.sh " + "'" + nickname + ":" + message + "'");
                    }
                }
                }
        }
        


 