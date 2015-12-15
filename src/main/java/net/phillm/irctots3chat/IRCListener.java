/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.phillm.irctots3chat;

import com.github.theholywaffle.teamspeak3.TS3Api;
import static org.pircbotx.Colors.removeFormattingAndColors;
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
        if (event.getMessage().startsWith(".ping")) {
            event.respond("Pong!");
        } else {
            String nickname = event.getUser().getNick();
            String message = removeFormattingAndColors(event.getMessage());
            
            TS3Bot ts3Bot = irctots3chat.getTS3();
            if (ts3Bot != null) {
                TS3Api api = ts3Bot.getAPI();
                if (api != null) {
                    api.sendChannelMessage(nickname + " : " + message);
                } else {
                    System.out.println("Can't send to TS3: got null api ref.");
                }
            } else {
                 System.out.println("Can't send to TS3: bot not initalsed yet.");
            }
            if (nickname.equalsIgnoreCase("skype")) {
                System.out.println("nick " + nickname + " is skype. not sending msg");
            } else {
                String sendToSkypeResult = irctots3chat.executeCommand(new String[]{"if [ -x ./skype-msg.sh]; then ./skype-msg.sh ", nickname + ": '" + message + "'; fi"}).trim();
                System.out.println("Got restult: " + sendToSkypeResult);
                if (sendToSkypeResult.equalsIgnoreCase("OK")) {
                    System.out.println("sent to skype: ./skype-msg.sh " + "'" + nickname + ": " + message + "'");
                } else {
                    System.out.println("Failed to send to skype: ./skype-msg.sh " + "'" + nickname + ": " + message + "'");
                }
            }
        }
    }
}
