package net.phillm.irctots3chat;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import java.util.Map;
import java.util.ArrayList;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.HashMap;

public class TS3Bot {

    private  TS3ApiAsync  api = null;
    private Map<Integer, String> uidsInChannel = new HashMap();
    public ArrayList<String> stripableTS3FormattingTags;

    public TS3Bot() throws InterruptedException {
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
        if (ts3ConfigInput != null) {
        final Map<String, String> ts3ConfigMap = (Map<String, String>) ts3ConfigParser.load(ts3ConfigInput);

        config.setHost(ts3ConfigMap.get("host"));

        final TS3Query query = new TS3Query(config);
        query.connect();

       // final TS3ApiAsync  api = query.getAsyncApi();
        //Api = api;
        
        api = query.getAsyncApi();

        api.selectVirtualServerById(1);

        api.login(ts3ConfigMap.get("username"), ts3ConfigMap.get("password"));

        api.setNickname(ts3ConfigMap.get("nick"));

        api.moveClient(api.whoAmI().get().getId(), api.getChannelByNameExact(ts3ConfigMap.get("channel"), true).get().getId());
        api.sendChannelMessage(api.whoAmI().get().getNickname() + " is active");

        api.registerAllEvents();
        api.addTS3Listeners(new TS3ChatBridgeListener(this));
        }
    }

    public String stripTS3FormattingTags(String message) {
        for (String tagtoStrip : stripableTS3FormattingTags) {
            message = message.replaceAll(tagtoStrip, "");
        }
        return message;
    }

    public TS3ApiAsync getAPI() {
        return api;
    }

    public  String nameTagStrip(String originalName) {

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
    public void addChannelUid(Integer ClientID, String ClientNick){
        uidsInChannel.put(ClientID, ClientNick);
    }
    
    public void removeChannelUid(Integer ClientID){
        uidsInChannel.remove(ClientID);
    }
}
