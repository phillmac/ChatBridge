package net.phillm.irctots3chat;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Colors;
import org.pircbotx.output.OutputChannel;

public class TS3ChatListener implements TS3Listener {

    private final TS3Bot ts3;
    private final TS3ApiAsync api;

    public TS3ChatListener(TS3Bot TS3) {

        ts3 = TS3;
        api = TS3.getAPI();
    }

    @Override
    public void onTextMessage(TextMessageEvent e) {
        System.out.println("onTextMessage fired");
        if (e.getTargetMode() == TextMessageTargetMode.CHANNEL) {
            String senderName = ts3.nameTagStrip(e.getInvokerName());

            try {
                if (!api.whoAmI().get().getNickname().equals(e.getInvokerName())) {

                    ArrayList<String> msgContents = new ArrayList(Arrays.asList(e.getMessage().split(" ")));

                    switch (msgContents.get(0)) {
                        case "!ping":
                            api.sendChannelMessage("pong");
                            break;
                        case "!ircconect":
                            irctots3chat.connectIRC();
                            break;
                        case "!findip":
                            List<Client> clientsList;
                            Boolean ipFound = false;
                            try {
                                clientsList = api.getClients().get();

                                for (Client client : clientsList) {
                                    if (api.getClientInfo(client.getId()).get().getIp().equals(msgContents.get(1))) {
                                        api.sendChannelMessage("Found client with ip " + msgContents.get(1) + " : " + client.getNickname());
                                        ipFound = true;
                                        break;
                                    }
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if (!ipFound) {
                                api.sendChannelMessage("Couldn't find a client with ip " + msgContents.get(1));
                            }
                            break;

                        case "!getircnick":
                            api.sendChannelMessage("IRC bot nick is " + irctots3chat.getIRCManger().getBots().first().getNick());
                            break;
                        default:

                            OutputChannel ircChannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel(irctots3chat.ircConfigMap.get("channel")).send();
                            StringBuilder messageBuilder = new StringBuilder();
                            messageBuilder.append(senderName);
                            messageBuilder.append(Colors.lookup(irctots3chat.ircConfigMap.get("messageseperatorcolor")));
                            messageBuilder.append(irctots3chat.ircConfigMap.get("messageseperator"));
                            messageBuilder.append(Colors.lookup(irctots3chat.ircConfigMap.get("messagecolor")));
                            messageBuilder.append(ts3.stripTS3FormattingTags(e.getMessage()));
                            
                            
                            ircChannel.message(messageBuilder.toString());
                            irctots3chat.executeCommand(new String[]{"./skype-msg.sh", "TS3: " + senderName + ": " + ts3.stripTS3FormattingTags(e.getMessage())});

                            break;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void onServerEdit(ServerEditedEvent e) {

    }

    @Override
    public void onClientMoved(ClientMovedEvent e) {
        ClientInfo movingClient = null;
        Integer movingClientId = e.getClientId();
        ServerQueryInfo localInfo = null;
        ChannelInfo channelInfo = null;

        try {
            movingClient = api.getClientInfo(movingClientId).get();
            localInfo = api.whoAmI().get();
            channelInfo = api.getChannelInfo(localInfo.getChannelId()).get();
        } catch (InterruptedException ex) {
            Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (movingClient != null && localInfo != null && channelInfo != null) {
            String originalClientName = movingClient.getNickname();
            String clientName = ts3.nameTagStrip(originalClientName);

            OutputChannel ircchannel;
            ircchannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel(irctots3chat.ircConfigMap.get("channel")).send();

            if ((movingClient.getChannelId() == localInfo.getChannelId()) && (!localInfo.getNickname().equals(originalClientName))) {
                ircchannel.message(clientName + " Joined Channel " + channelInfo.getName());
                ts3.addChannelUid(movingClientId, clientName);
            } else if ((movingClient.getChannelId() != localInfo.getChannelId()) && (!localInfo.getNickname().equals(originalClientName))) {
                if (ts3.getuidsInChannel().keySet().contains(movingClientId)) {
                    try {
                        ircchannel.message(clientName + " Moved to Channel " + api.getChannelInfo(movingClient.getChannelId()).get().getName());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ts3.removeChannelUid(movingClientId);
                } else {
                    System.out.println("onClientMoved fired: Unrelated channel");
                }
            } else if (localInfo.getNickname().equals(originalClientName)) {
                try {
                    ircchannel.message(clientName + " IRC Bot moved to Channel " + api.getChannelInfo(movingClient.getChannelId()).get().getName());
                } catch (InterruptedException ex) {
                    Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void onClientLeave(ClientLeaveEvent e) {
        Integer leavingClientId = e.getClientId();

        if (ts3.getuidsInChannel().keySet().contains(leavingClientId)) {
            OutputChannel ircchannel;
            ircchannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel(irctots3chat.ircConfigMap.get("channel")).send();
            ircchannel.message(ts3.getuidsInChannel().get(leavingClientId) + " Left the channel");
            ts3.removeChannelUid(leavingClientId);

        } else {
            System.out.println("onClientLeave fired: Unrelated client. ID: " + leavingClientId);

        }

    }

    @Override
    public void onClientJoin(ClientJoinEvent e) {
        ClientInfo joiningClient = null;
        Integer joiningClientId = e.getClientId();
        ServerQueryInfo localInfo = null;
        ChannelInfo channelInfo = null;

        System.out.println("new client ID: " + joiningClientId.toString() + " Type: " + e.getClientType());
        if (e.getClientType() == 0) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
            }

            List<Client> clientsList = null;
            try {
                clientsList = api.getClients().get();
            } catch (InterruptedException ex) {
                Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
            List<Integer> clientIdsList = new ArrayList();
            if (clientsList != null) {
                for (Client client : clientsList) {
                    clientIdsList.add(client.getId());
                }
            }
            if (clientIdsList.contains(joiningClientId)) {
                System.out.println("New client with ID: " + joiningClientId + " is valid");

                try {
                    joiningClient = api.getClientInfo(joiningClientId).get();
                    localInfo = api.whoAmI().get();
                    channelInfo = api.getChannelInfo(localInfo.getChannelId()).get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (joiningClient != null && localInfo != null && channelInfo != null) {
                    String originalClientName = e.getClientNickname();
                    String clientName = ts3.nameTagStrip(originalClientName);

                    if ((joiningClient.getChannelId() == localInfo.getChannelId()) && (!localInfo.getNickname().equals(originalClientName))) {
                        OutputChannel ircchannel;

                        ircchannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel(irctots3chat.ircConfigMap.get("channel")).send();
                        ircchannel.message(clientName + " Joined Channel " + channelInfo.getName());
                        ts3.addChannelUid(joiningClientId, clientName);
                    } else {
                        System.out.println("onClientJoin fired: Unrelated client");
                    }
                }
            } else {
                System.out.println("New client with ID: " + joiningClientId + " left before delay finnished");
            }
        } else {
            System.out.println("New client not of Type 0");
        }
    }

    @Override
    public void onChannelEdit(ChannelEditedEvent e) {

    }

    @Override
    public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {

    }

    @Override
    public void onChannelCreate(ChannelCreateEvent e) {

    }

    @Override
    public void onChannelDeleted(ChannelDeletedEvent e) {

    }

    @Override
    public void onChannelMoved(ChannelMovedEvent e) {

    }

    @Override
    public void onChannelPasswordChanged(ChannelPasswordChangedEvent e) {

    }

    @Override
    public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent pkue) {

    }
}
