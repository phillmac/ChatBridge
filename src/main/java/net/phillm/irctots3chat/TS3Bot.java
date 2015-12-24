/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.phillm.irctots3chat;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo;
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Level;
import org.pircbotx.output.OutputChannel;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Phillip
 */
public class TS3Bot {

    private final TS3Api Api;
    private Map<Integer, String> uidsInChannel = new HashMap();
    public ArrayList<String> stripableTS3FormattingTags;

    public TS3Bot() {
        stripableTS3FormattingTags = new ArrayList();
        stripableTS3FormattingTags.add("(\\[URL\\])");
        stripableTS3FormattingTags.add("(\\[\\/URL\\])");
        stripableTS3FormattingTags.add("(\\[url\\])");
        stripableTS3FormattingTags.add("(\\[\\/url\\])");

        final TS3Config config = new TS3Config();
        //config.setDebugLevel(Level.ALL);
        InputStream ts3ConfigInput = null;
        try {
            ts3ConfigInput = new FileInputStream(new File("ts3config.yml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Could not find ts3config.yml. Please check that it exists.");
        }

        Yaml ts3ConfigParser = new Yaml();
        final Map<String, String> ts3ConfigMap = (Map<String, String>) ts3ConfigParser.load(ts3ConfigInput);

        config.setHost(ts3ConfigMap.get("host"));

        final TS3Query query = new TS3Query(config);
        query.connect();

        final TS3Api api = query.getApi();
        Api = api;

        api.selectVirtualServerById(1);

        api.login(ts3ConfigMap.get("username"), ts3ConfigMap.get("password"));

        api.setNickname(ts3ConfigMap.get("nick"));

        api.moveClient(api.whoAmI().getId(), api.getChannelByNameExact(ts3ConfigMap.get("channel"), true).getId());
        api.sendChannelMessage(api.whoAmI().getNickname() + " is active");

        api.registerAllEvents();
        api.addTS3Listeners(new TS3Listener() {

            @Override
            public void onTextMessage(TextMessageEvent e) {
                System.out.println("onTextMessage fired");
                if (e.getTargetMode() == TextMessageTargetMode.CHANNEL) {
                    String senderName = nameTagStrip(e.getInvokerName());

                    if (!api.whoAmI().getNickname().equals(e.getInvokerName())) {

                        ArrayList<String> msgContents = new ArrayList(Arrays.asList(e.getMessage().split(" ")));

                        switch (msgContents.get(0)) {
                            case "!ping":
                                api.sendChannelMessage("pong");
                                break;
                            case "!findip":
                                List<Client> clientsList = api.getClients();
                                Boolean ipFound = false;

                                for (Client client : clientsList) {
                                    if (api.getClientInfo(client.getId()).getIp().equals(msgContents.get(1))) {
                                        api.sendChannelMessage("Found client with ip " + msgContents.get(1) + " : " + client.getNickname());
                                        ipFound = true;
                                    }
                                }
                                if (ipFound) {
                                    api.sendChannelMessage("Couldn't find a client with ip " + msgContents.get(1));
                                }
                                break;

                            case "!getircnick":
                                api.sendChannelMessage("IRC bot nick is " + irctots3chat.getIRCManger().getBots().first().getNick());
                                break;
                            default:

                                OutputChannel ircChannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel("#mcserverchat-test").send();
                                String message = stripTS3FormattingTags(e.getMessage());
                                ircChannel.message(senderName + " : " + message);
                                irctots3chat.executeCommand(new String[]{"./skype-msg.sh", "TS3: " + senderName + ": " + message});

                                break;
                        }
                    }
                }
            }

            @Override

            public void onServerEdit(ServerEditedEvent e) {

            }

            @Override
            public void onClientMoved(ClientMovedEvent e) {
                ClientInfo movingClient;
                Integer movingClientId = e.getClientId();
                ServerQueryInfo localInfo;
                ChannelInfo channelInfo;

                movingClient = api.getClientInfo(movingClientId);
                localInfo = api.whoAmI();
                channelInfo = api.getChannelInfo(localInfo.getChannelId());

                String originalClientName = movingClient.getNickname();
                String clientName = nameTagStrip(originalClientName);

                OutputChannel ircchannel;
                ircchannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel(irctots3chat.ircConfigMap.get("channel")).send();

                if ((movingClient.getChannelId() == localInfo.getChannelId()) && (!localInfo.getNickname().equals(originalClientName))) {
                    ircchannel.message(clientName + " Joined Channel " + channelInfo.getName());
                    uidsInChannel.put(movingClientId, clientName);
                } else if ((movingClient.getChannelId() != localInfo.getChannelId()) && (!localInfo.getNickname().equals(originalClientName))) {
                    if (uidsInChannel.keySet().contains(movingClientId)) {
                        ircchannel.message(clientName + " Moved to Channel " + api.getChannelInfo(movingClient.getChannelId()).getName());
                        uidsInChannel.remove(movingClientId);
                    } else {
                        System.out.println("onClientMoved fired: Unrelated channel");
                    }
                } else if (localInfo.getNickname().equals(originalClientName)) {
                    ircchannel.message(clientName + " IRC Bot moved to Channel " + api.getChannelInfo(movingClient.getChannelId()).getName());
                }
            }

            @Override
            public void onClientLeave(ClientLeaveEvent e) {
                Integer leavingClientId = e.getClientId();

                if (uidsInChannel.keySet().contains(leavingClientId)) {
                    OutputChannel ircchannel;
                    ircchannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel(irctots3chat.ircConfigMap.get("channel")).send();
                    ircchannel.message(uidsInChannel.get(leavingClientId) + " Left the channel");
                    uidsInChannel.remove(leavingClientId);

                } else {
                    System.out.println("onClientLeave fired: Unrelated client");

                }

            }

            @Override
            public void onClientJoin(ClientJoinEvent e) {
                ClientInfo joiningClient;
                Integer joiningClientId = e.getClientId();
                ServerQueryInfo localInfo;
                ChannelInfo channelInfo;

                System.out.println("new client ID: " + joiningClientId.toString());
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TS3Bot.class.getName()).log(Level.SEVERE, null, ex);
                }

                List<Client> clientsList = api.getClients();
                List<Integer> clientIdsList = new ArrayList();
                for (Client client : clientsList) {
                    clientIdsList.add(client.getId());
                }

                if (clientIdsList.contains(joiningClientId)) {
                    System.out.println("New client with ID: " + joiningClientId + " is valid");

                    joiningClient = api.getClientInfo(joiningClientId);
                    localInfo = api.whoAmI();
                    channelInfo = api.getChannelInfo(localInfo.getChannelId());
                    String originalClientName = e.getClientNickname();
                    String clientName = nameTagStrip(originalClientName);

                    if ((joiningClient.getChannelId() == localInfo.getChannelId()) && (!localInfo.getNickname().equals(originalClientName))) {
                        OutputChannel ircchannel;

                        ircchannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel(irctots3chat.ircConfigMap.get("channel")).send();
                        ircchannel.message(clientName + " Joined Channel " + channelInfo.getName());
                        uidsInChannel.put(joiningClientId, clientName);
                    } else {
                        System.out.println("onClientJoin fired: Unrelated client");
                    }
                } else {
                    System.out.println("New client with ID: " + joiningClientId + " left");
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
        }
        );
    }

    private String stripTS3FormattingTags(String message) {
        for (String tagtoStrip : stripableTS3FormattingTags) {
            message = message.replaceAll(tagtoStrip, "");
        }
        return message;
    }

    public TS3Api getAPI() {
        return Api;

    }

    public String nameTagStrip(String originalName) {

        String nameTagStriped;
        int beginStrip;
        int endStrip;
        beginStrip = originalName.indexOf("|") + 1;
        if (beginStrip != -1) {
        } else {
            beginStrip = 0;
        }
        endStrip = originalName.indexOf("[");
        if (endStrip != -1) {
        } else {
            endStrip = originalName.length();
        }
        nameTagStriped = originalName.substring(beginStrip, endStrip).trim();
        while (nameTagStriped.startsWith(".")) {
            nameTagStriped = nameTagStriped.substring(1);
        }
        return nameTagStriped;
    }

}
