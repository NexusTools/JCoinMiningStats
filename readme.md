#JBitMiningStats

###About
*JBitMiningStats* is an **Android** application (coded towards API Level 8+, aka Android 2.2+, using Java). This application allows you to see your [Bitcoin](http://bitcoin.org/en/) mining statistics.

###Features
Currently the only pool supported is [Slush's Pool](http://mining.bitcoin.cz/), and in an indefinite time period there likely will be more to be added/supported.
* Visual notification of value change - everytime a variable increases or decreases an effect will happen where it fades from green or red respectively.
* [Slush's Pool](http://mining.bitcoin.cz/)
* [Mt.Gox](https://mtgox.com/) BTC Currency Exchange

###Slush's Pool
Once you've entered your API key from Slush's Pool, you'll automatically be able to see the following:
* Pool statistics - your worker rate, confirmed/unconfirmed rewards (including confirmed/unconfirmed namecoin, and your estimated reward).
* Miner statistics - each miner listed in your pool statistics is listed out into a table, displaying if it's alive, the name, rate, shares, and score of each miner.
* Block statistics - upon your manual input of choosing to "Show Blocks", the table will switch over to the current block confirmations from your pool statistics. The block table will show the ID, confirmations, reward, namecoin rewad, score and shares of each block.


###Mt.Gox
Once enabled, you'll be able to have any normal BTC currency be exchanged automatically based off Mt.Gox prices into your selected currency. When Mt.Gox is enabled you'll also see a new entry in the interface showing the current BTC price.

Currently the currencies supported are as follows:
* USD
* AUD
* CAD
* CHF
* CNY
* DKK
* EUR
* GBP
* HKD
* JPY
* NZD
* PLN
* RUB
* SEK
* SGD
* THB
* NOK
* CZK

(If any new currencies are to be added into Mt.Gox a notification/pull request would be appreciated.)

###Customization
Along with this application I've intregrated a lot of customization that easily allows you to control when, where, and how the data you're retriving is handled.
*Not all of these options are available for every provider, however, the items listed work for the majority.*
* Auto-connect - enables/disables automatically connecting to any of the provided services, in the case that you disable automatically connecting you'll have to instead use the menu item "Connect Now" to manually connect whenever you want an update from the providers.
* Connection delays - only applies if the aforementioned auto-connect is enabled; at the specified interval of the connection delay (default 15,000 milliseconds aka 15 seconds) a new connection to fetch the providers data will be made. However, lower than the default option is highly unrecommended as too common of a connection may result of being blocked by the provider.
* Toggling hash-rate units - If you find it redundant to show the hashrate units you can toggle the hashrate appendage on/off.
* Parsing messages - whenever a connection is made, and the option to show parsing messages is enabled, a toast will be displayed saying the providers JSON has been fetched.
* Backup/forced user-agent - If for some reason your phone's user-agent isn't working, or whatever the case might be, you can set your backup user-agent and there's an option to force-use that backup user-agent.
* Setting of any and all API domains - Incase there's a change in the domain of which the JSON data is fetched, each API domain has been added as a changeable option. Not only does this allow you to even use your own JSON provider, but it makes sure that if the application hasn't been updated, you don't have to worry about not having your provider information.
* And more - with *JBitMiningStats*, it's about being dynamic, customizable, and easy to follow/use.
