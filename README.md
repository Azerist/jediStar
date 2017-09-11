# Discord Bot JediStar

## Requirements

All you'll need is a computer connected to the Internet. Any OS that can run Java will do.

No need to be a computer crack, the installation is very easy.

Of course, if you want the boot to be available 24/7, the computer that runs it will need to stay up 24/7.
You can also rent a small server, for example i was able to rent one for less than 4€/month.

## Installing the bot

1. You'll need to install a "Java runtime Environment (JRE)" with Java version 1.8. You can download a JRE from Oracle's website [here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).
2. Download the latest version of the application from the "runnable" directory (that's the .jar file). Along with it, download one of the settings files in the language that you like most.
3. Put these two files in the same directory on your computer. Rename the settings file into "settings.json" (remove the language suffix at the end of the file).
4. Follow [this quick tutorial](https://github.com/reactiflux/discord-irc/wiki/Creating-a-discord-bot-&-getting-a-token) to create a Discord bot and get a "token". This token is what helps the application connecting to your Discord server.
5. Open the settings.json file you downloaded at step 3. In the first field, put the token you got from Discord at step 4 like in the following example : 	`"discordToken": "MzQyNTk4ODE5MjQzODg0NTU1.DGR9iA.rBI0QHIdCavRVi_fdZoFrh59vK4",`
6. Inside this same file, you should customize the "bot Admins" section. This defines who will be allowed to run administrator commands from Discord. You may use group names (as they are defined inside Discord) or individual user IDs (the 4-digits ID you can see in Discord).
7. You may customize anything else you like in this settings.json file, like the keywords used by the commands, the raid names, etc... But be careful not to change its structure, or the bot would fail to work correctly. 
**Please note that i would be really interested if you gave me translated versions in your own language !**
8. Launch the bot following the next section.

## Launching the application

You need to launch the application in order to have it answer your commands inside Discord.
There are two ways to do it :

* For those who are not familiar with computers, on most computers, double-clicking on the .jar file will be enough.
* For those who are a bit more skilled with computers, and who want to have feedback from the application, you may open a terminal window and type the following command : `java -jar JediStarBot-X.X.X.jar`

## Using the bot

If anything went well in the previous steps, your bot should now be able to answer your commands.

Right now, the available commands are (unless you customized them in the settings.json) :
* !raid
* !mods

Enjoy !

## Contact
If you have any question, problem or suggestion, please join this Discord Server : https://discord.gg/GmQSAn
