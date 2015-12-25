package net.phillm.irctots3chat;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.pircbotx.Colors.removeFormattingAndColors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class IRCListener extends ListenerAdapter {

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        System.out.println("IRC onGenericMessage fired");
        if (event.getMessage().startsWith(".ping")) {
            event.respond("Pong!");
        } else if (event.getMessage().startsWith(".ts3isconnected")) {
            try {
                if (irctots3chat.getTS3().getAPI().getConnectionInfo().getUninterruptibly(5000, TimeUnit.SECONDS).getPing() < 2000) {
                    event.respond("Connected!");
                } else {
                    event.respond(" Not Connected!");
                }
            } catch (TimeoutException ex) {
                event.respond(" Not Connected!");
            }

        } else {
            String nickname = event.getUser().getNick();
            String message = removeFormattingAndColors(event.getMessage());

            TS3Bot ts3Bot = irctots3chat.getTS3();
            if (ts3Bot != null) {
                TS3ApiAsync api = ts3Bot.getAPI();
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
                String sendToSkypeResult = irctots3chat.executeCommand(new String[]{"./skype-msg.sh", nickname + ": " + message}).trim();
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
