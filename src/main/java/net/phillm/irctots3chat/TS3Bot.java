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
import java.util.logging.Level;
import org.pircbotx.output.OutputChannel;

/**
 *
 * @author Phillip
 */
public class TS3Bot {
    private final TS3Api Api;
    public TS3Bot(){
 
		final TS3Config config = new TS3Config();
		config.setHost("");
		config.setDebugLevel(Level.ALL);
		config.setLoginCredentials("", "");

		final TS3Query query = new TS3Query(config);
		query.connect();

		final TS3Api api = query.getApi();
                Api = api;
                
		api.selectVirtualServerById(1);
		api.setNickname("IRC");
                api.moveClient(api.getChannelByName("Wayne Manor").getId());
		api.sendChannelMessage(api.whoAmI().getNickname() +  " is active");

		api.registerAllEvents();
		api.addTS3Listeners(new TS3Listener() {

                        @Override
			public void onTextMessage(TextMessageEvent e) {
				if (e.getTargetMode() == TextMessageTargetMode.CHANNEL ) {
                                    String originalName; 
                                    String nameTagStrip;                                                                                         
                                    int beginStrip;
                                    int endStrip;
                                    originalName = e.getInvokerName(); 										
                                    beginStrip = originalName.indexOf("|")+1; 
                                    if (beginStrip != -1) {
                                    } else {
                                        beginStrip = 0;
                                    }
                                    endStrip = originalName.indexOf("[");      
                                    if (endStrip != -1) {
                                    } else {
                                        endStrip = originalName.length();
                                    }
                                    nameTagStrip = originalName.substring(beginStrip,endStrip).trim();
                                    switch (e.getMessage()) {
                                        case "!ping":
                                            api.sendChannelMessage("pong");
                                            break;
                                        case "!getircnick":
                                            api.sendChannelMessage("IRC bot nick is " + irctots3chat.getIRCManger().getBots().first().getNick());
                                            break;
                                        default:
                                            if (! api.whoAmI().getNickname().equals(e.getInvokerName()) ){
                                                
                                                OutputChannel ircchannel =  irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel("#mcserverchat-test").send();
                                              
                                                ircchannel.message(nameTagStrip + " : " + e.getMessage());
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
                            ClientInfo joiningClient;
                            ServerQueryInfo localInfo;
                            ChannelInfo channelInfo;
                                    
                            joiningClient = api.getClientInfo( e.getClientId());
                            localInfo = api.whoAmI();
                            channelInfo = api.getChannelInfo(localInfo.getChannelId());
                            
                           String originalName; 
                           String nameTagStrip;                                                                                         
                           
                           int beginStrip;
                           int endStrip;
                           
                           originalName = joiningClient.getNickname();
                            
                            beginStrip = originalName.indexOf("|")+1;
                            if (beginStrip == -1)
                                beginStrip =  0;
                            
                            endStrip = originalName.indexOf("[");
                            if (endStrip == -1)
                                endStrip =  originalName.length();
                            
                            nameTagStrip =  originalName.substring(beginStrip,endStrip).trim();
                                    
                         if((joiningClient.getChannelId() == localInfo.getChannelId()) &&(! localInfo.getNickname().equals(originalName))){
                            //api.sendChannelMessage("Welcome to Wayne Manor " + joinerNickname.substring(joinerNickname.indexOf("|")+1,joinerNickname.indexOf("[")).trim() + ". May I take your coat?");
			OutputChannel ircchannel =  irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel("#mcserverchat-test").send();
                                               
                                                ircchannel.message(nameTagStrip + " Joined Channel " + channelInfo.getName());
                         }
                         
                        }

                        @Override
			public void onClientLeave(ClientLeaveEvent e) {
                            
                            ClientInfo leavingClient;
                            ServerQueryInfo localInfo;
                            ChannelInfo channelInfo;
                                    
                           // leavingClient = api.getClientInfo( e.getClientId());
                           // localInfo = api.whoAmI();
                           // channelInfo = api.getChannelInfo(localInfo.getChannelId());
                           
                           String originalName; 
                           String nameTagStrip;                                                                                         
                           
                           int beginStrip;
                           int endStrip;
                           
                           //originalName = leavingClient.getNickname();
                            
                          //  beginStrip = originalName.indexOf("|")+1;
                           // if (beginStrip == -1)
                                beginStrip =  0;
                            
                           // endStrip = originalName.indexOf("[");
                            //if (endStrip == -1)
                             //   endStrip =  originalName.length();
                            
                           // nameTagStrip =  originalName.substring(beginStrip,endStrip).trim();
                            
                         //if((leavingClient.getChannelId() == localInfo.getChannelId()) &&(! localInfo.getNickname().equals(originalName))){
                            //api.sendChannelMessage("Welcome to Wayne Manor " + joinerNickname.substring(joinerNickname.indexOf("|")+1,joinerNickname.indexOf("[")).trim() + ". May I take your coat?");
			//OutputChannel ircchannel =  irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel("#mcserverchat-test").send();
                                               
                         //                       ircchannel.message(nameTagStrip + " Left Channel " + channelInfo.getName());
                        // }
                            
                            
			}

                        @Override
			public void onClientJoin(ClientJoinEvent e) {ClientInfo joiningClient;
                            ServerQueryInfo localInfo;
                            ChannelInfo channelInfo;
                                    
                            joiningClient = api.getClientInfo( e.getClientId());
                            localInfo = api.whoAmI();
                            channelInfo = api.getChannelInfo(localInfo.getChannelId());
                            
                           String originalName; 
                           String nameTagStrip;                                                                                         
                           
                           int beginStrip;
                           int endStrip;
                           
                           originalName = joiningClient.getNickname();
                            
                            beginStrip = originalName.indexOf("|")+1;
                            if (beginStrip == -1)
                                beginStrip =  0;
                            
                            endStrip = originalName.indexOf("[");
                            if (endStrip == -1)
                                endStrip =  originalName.length();
                            
                            nameTagStrip =  originalName.substring(beginStrip,endStrip).trim();
                                    
                         if((joiningClient.getChannelId() == localInfo.getChannelId()) &&(! localInfo.getNickname().equals(originalName))){
                            //api.sendChannelMessage("Welcome to Wayne Manor " + joinerNickname.substring(joinerNickname.indexOf("|")+1,joinerNickname.indexOf("[")).trim() + ". May I take your coat?");
			OutputChannel ircchannel =  irctots3chat.getIRCManger().getBots().first().getUserChannelDao().getChannel("#mcserverchat-test").send();
                                               
                                                ircchannel.message(nameTagStrip + " Joined Channel " + channelInfo.getName());
                         }
                            
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
    public TS3Api getAPI(){
     return Api;
    
    }
}