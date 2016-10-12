package net.phillm.chatbridge;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import java.util.Map;
import java.util.ArrayList;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static net.phillm.chatbridge.ChatBridge.extractConfig;
import org.apache.commons.lang3.StringUtils;

public class TS3Bot {

    private TS3ApiAsync api = null;
    private Map<Integer, String> uidsInChannel = new HashMap();
    public ArrayList<String> stripableTS3BBcode;

    public TS3Bot() throws InterruptedException {
        stripableTS3BBcode = new ArrayList();
        stripableTS3BBcode.add("(\\[URL\\])");
        stripableTS3BBcode.add("(\\[\\/URL\\])");
        stripableTS3BBcode.add("(\\[url\\])");
        stripableTS3BBcode.add("(\\[\\/url\\])");

        final TS3Config config = new TS3Config();
        //config.setDebugLevel(Level.ALL);
        InputStream ts3ConfigInput = null;
        try {
            ts3ConfigInput = new FileInputStream(new File("ts3config.yml"));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find ts3config.yml");
            System.out.println("Extracting ts3config.yml");
            extractConfig("ts3config.yml", "ts3config.yml");
        }

        Yaml ts3ConfigParser = new Yaml();
        if (ts3ConfigInput != null) {
            final Map<String, String> ts3ConfigMap = (Map<String, String>) ts3ConfigParser.load(ts3ConfigInput);
            if (!ts3ConfigMap.get("host").equals("")) {
                config.setHost(ts3ConfigMap.get("host"));
                if (!ts3ConfigMap.get("queryport").equals("")) {
                    Integer portNo = Integer.parseInt(ts3ConfigMap.get("queryport"));
                    config.setQueryPort(portNo);
                    System.out.println("TS3 query port: " + portNo.toString());

                } else {
                    config.setQueryPort(10011);
                }

                final TS3Query query = new TS3Query(config);
                query.connect();

                // final TS3ApiAsync  api = query.getAsyncApi();
                //Api = api;
                api = query.getAsyncApi();
                if (!ts3ConfigMap.get("voiceport").equals("")) {

                    Integer voicePort = Integer.parseInt(ts3ConfigMap.get("voiceport"));
                    try {
                        api.selectVirtualServerByPort(voicePort);
                    } catch (TS3CommandFailedException e) {
                        api.selectVirtualServerById(1);
                    }
                } else {
                    try {
                        api.selectVirtualServer(api.getVirtualServers().get().get(1));
                    } catch (TS3CommandFailedException e) {
                        api.selectVirtualServerById(1);
                    }
                }

                CommandFuture<Boolean> login = api.login(ts3ConfigMap.get("username"), ts3ConfigMap.get("password"));

                try {
                    login.get(30, TimeUnit.SECONDS);
                } catch (TimeoutException ex) {
                    System.out.println("Timed out while trying to login");
                }

                if (login.isSuccessful()) {

                    api.setNickname(ts3ConfigMap.get("nick"));

                    int ts3ChannelId = 0;
                    String ts3CfgChnnlVal = ts3ConfigMap.get("channel");
                    boolean ts3ChannelIdValid = false;

                    if (ts3CfgChnnlVal.equals("")) {
                        System.out.println("Channel setting blank, not switching channels");
                    } else {
                        if (StringUtils.isNumeric(ts3CfgChnnlVal)) {
                            ts3ChannelId = Integer.parseInt(ts3CfgChnnlVal);
                            ts3ChannelIdValid = true;
                        } else {
                            CommandFuture<Channel> channelNameSeach = api.getChannelByNameExact(ts3CfgChnnlVal, true);
                            try {
                                ts3ChannelId = channelNameSeach.get(30, TimeUnit.SECONDS).getId();
                            } catch (TimeoutException ex) {
                                System.out.println("Timed out while searcghing for channel name '" + ts3CfgChnnlVal + "'");
                            }
                            if (channelNameSeach.isSuccessful()) {
                                ts3ChannelIdValid = true;
                            } else {
                                System.out.println("could not find channel name '" + ts3CfgChnnlVal + "'");
                            }
                        }
                        if (ts3ChannelIdValid) {
                            api.moveClient(api.whoAmI().get().getId(), ts3ChannelId);
                        }

                        api.sendChannelMessage(api.whoAmI().get().getNickname() + " is active");
                        api.registerAllEvents();
                        api.addTS3Listeners(new TS3ChatListener(this));
                    }
                } else {
                    System.out.println("Could not login, Check that the login details are set correctly in ts3config.yml");
                }
            } else {
                System.out.println("Check that the host is set correctly in ts3config.yml");
            }
        }
    }

    public String stripTS3FormattingTags(String message) {
        for (String tagtoStrip : stripableTS3BBcode) {
            message = message.replaceAll(tagtoStrip, "");
        }
        return message;
    }
    
    public String remap_formating(Map formatRemapping, String Message) {
     
       return ""; //to be implemented 
    }

    public TS3ApiAsync getAPI() {
        return api;
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

    public Map<Integer, String> getuidsInChannel() {
        return uidsInChannel;
    }

    public void setuidsInChannel(Map<Integer, String> Uids) {
        uidsInChannel = Uids;
    }

    public void addChannelUid(Integer ClientID, String ClientNick) {
        uidsInChannel.put(ClientID, ClientNick);
    }

    public void removeChannelUid(Integer ClientID) {
        uidsInChannel.remove(ClientID);
    }
}
