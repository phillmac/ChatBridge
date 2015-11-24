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
import com.github.theholywaffle.teamspeak3.api.event.ChannelCreateEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDeletedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDescriptionEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelPasswordChangedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientLeaveEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ServerEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo;
import java.util.ArrayList;
import java.util.logging.Level;
import org.pircbotx.output.OutputChannel;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.Map;

/**
 *
 * @author Phillip
 */
public class TS3Bot {

    private final TS3Api Api;
    private ArrayList<Integer> uidsInChannelBefore;

    public TS3Bot() {

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
        Map<String, String> ts3ConfigMap = (Map<String, String>) ts3ConfigParser.load(ts3ConfigInput);

        config.setHost(ts3ConfigMap.get("host"));
        config.setLoginCredentials(ts3ConfigMap.get("username"), ts3ConfigMap.get("password"));

        final TS3Query query = new TS3Query(config);
        query.connect();

        final TS3Api api = query.getApi();
        Api = api;

        api.selectVirtualServerById(1);
        api.setNickname(ts3ConfigMap.get("nick"));

        api.moveClient(api.whoAmI().getId(), api.getChannelByNameExact(ts3ConfigMap.get("channel"), true).getId());
        api.sendChannelMessage(api.whoAmI().getNickname() + " is active");

        api.registerAllEvents();
        api.addTS3Listeners(new TS3Listener() {

            @Override
            public void onTextMessage(TextMessageEvent e) {
                System.out.println("onTextMessage fired");
                if (e.getTargetMode() == TextMessageTargetMode.CHANNEL) {
                    String originalName;
                    String nameTagStrip;
                    int beginStrip;
                    int endStrip;
                    originalName = e.getInvokerName();
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
                    nameTagStrip = originalName.substring(beginStrip, endStrip).trim();
                    switch (e.getMessage()) {
                        case "!ping":
                            api.sendChannelMessage("pong");
                            break;
                        case "!getircnick":
                            api.sendChannelMessage("IRC bot nick is " + irctots3chat.getIRCManger().getBots().first().getNick());
                            break;
                        default:
                            if (!api.whoAmI().getNickname().equals(e.getInvokerName())) {

                                OutputChannel ircchannel = irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel("#mcserverchat-test").send();
                                String message = e.getMessage();
                                ircchannel.message(nameTagStrip + " : " + message);
                                irctots3chat.executeCommand("./skype-msg.sh " + "'" + nameTagStrip + " : " + message + "'");
                            }
                            break;
                    }
                }
            }

            @Override
            public void onServerEdit(ServerEditedEvent e) {

            }

            @Override
            public void onClientMoved(ClientMovedEvent e) {
                System.out.println("onClientMoved fired");
                ClientInfo movingClient;
                           // ServerQueryInfo localInfo;
                //ChannelInfo channelInfo;

                movingClient = api.getClientInfo(e.getClientId());
                           // localInfo = api.whoAmI();
                // channelInfo = api.getChannelInfo(localInfo.getChannelId());

                String originalName;
                String nameTagStrip;
                int beginStrip;
                int endStrip;

                originalName = movingClient.getNickname();

                beginStrip = originalName.indexOf("|") + 1;
                if (beginStrip == -1) {
                    beginStrip = 0;
                }

                endStrip = originalName.indexOf("[");
                if (endStrip == -1) {
                    endStrip = originalName.length();
                }

                nameTagStrip = originalName.substring(beginStrip, endStrip).trim();

                OutputChannel ircchannel;
                    //        ircchannel =  irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel("#mcserverchat-test").send();

                        // if((movingClient.getChannelId() == localInfo.getChannelId()) &&(! localInfo.getNickname().equals(originalName))){
                //api.sendChannelMessage("Welcome to Wayne Manor " + joinerNickname.substring(joinerNickname.indexOf("|")+1,joinerNickname.indexOf("[")).trim() + ". May I take your coat?");
                // ircchannel.message(nameTagStrip + " Joined Channel " + channelInfo.getName());
                // }
                //if((movingClient.getChannelId() != localInfo.getChannelId()) &&(! localInfo.getNickname().equals(originalName))){
                             //  ircchannel.message(nameTagStrip + " Moved to Channel " + api.getChannelInfo(movingClient.getChannelId()).getName());
                // }
            }

            @Override
            public void onClientLeave(ClientLeaveEvent e) {
                System.out.println("onClientLeave fired");
            }

            @Override
            public void onClientJoin(ClientJoinEvent e) {
                System.out.println("onClientJoin fired");
            }

            @Override
            public void onChannelEdit(ChannelEditedEvent e) {

            }

            @Override
            public void onChannelDescriptionChanged(
                    ChannelDescriptionEditedEvent e) {

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
        });
    }

    public TS3Api getAPI() {
        return Api;

    }
}
