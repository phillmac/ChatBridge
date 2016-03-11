package net.phillm.chatbridge;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.pircbotx.Colors.removeFormattingAndColors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class IRCListener extends ListenerAdapter {

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        System.out.println("IRC onGenericMessage fired");
        
        ArrayList<String> msgContents = new ArrayList(Arrays.asList(event.getMessage().split(" ")));

        switch (msgContents.get(0)) {
            case ".ping":
                event.respond("Pong!");
                break;
            case ".connectts3":
                ChatBridge.connectTS3();
                break;
            case ".ts3isconnected":
                try {
                    if (ChatBridge.getTS3().getAPI().getConnectionInfo().getUninterruptibly(5000, TimeUnit.SECONDS).getPing() < 2000) {
                        event.respond(" Connected!");
                    } else {
                        event.respond(" Not Connected!");
                    }
                } catch (TimeoutException ex) {
                    event.respond(" Not Connected!");
                }
            break;

            default:
                String nickname = event.getUser().getNick();
                String message = removeFormattingAndColors(event.getMessage());

                TS3Bot ts3Bot = ChatBridge.getTS3();
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
                    String sendToSkypeResult = ChatBridge.executeCommand(new String[]{"./skype-msg.sh", nickname + ": " + message}).trim();
                    System.out.println("Got restult: " + sendToSkypeResult);
                    if (sendToSkypeResult.equalsIgnoreCase("OK")) {
                        System.out.println("sent to skype: ./skype-msg.sh " + "'" + nickname + ": " + message + "'");
                    } else {
                        System.out.println("Failed to send to skype: ./skype-msg.sh " + "'" + nickname + ": " + message + "'");
                    }
                }
                break;
        }
    }
}
