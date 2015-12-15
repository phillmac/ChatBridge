#!/usr/bin/env bash
#
# Example shell script for sending a message into sevabot
#
# Give command line parameters [chat id] and [message].
# The message is md5 signed with a shared secret specified in settings.py
# Then we use curl do to the request to sevabot HTTP interface.
#
#

chat=""
secret=""
skypeaddress=""
msg=$1

if [ -x skype-msg.settings ]; then . skype-msg.settings; fi

if [ -z "$chat" ] || [ -z "$secret" ] || [ -z  "$skypeaddress" ]; then
  echo "Please check your settings."

  if [ -e skype-msg.settings ] && [ ! -x skype-msg.settings ]; then
    echo "skype-msg.settings is not executable."
  fi

fi

md5=`echo -n "$chat$msg$secret" | md5sum`

#md5sum prints a '-' to the end. Let's get rid of that.
for m in $md5; do
    break
done
#cmd="curl $skypeaddress --data-urlencode chat=\"$chat\" --data-urlencode msg=\"$msg\" --data-urlencode md5=\"$m\""
#echo $cmd
curl $skypeaddress --data-urlencode chat="$chat" --data-urlencode msg="$msg" --data-urlencode md5="$m"