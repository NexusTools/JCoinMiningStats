package net.nexustools.jcoinminingstats.util;

import net.nexustools.jcoinminingstats.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Settings {
	public static enum ActivePoolView {
		Slush, WeMineLTC
	};
	
	public final static String SLUSHS_ACCOUNT_URL = "https://mining.bitcoin.cz/accounts/profile/json/";
	public final static String SLUSHS_BLOCK_URL = "https://mining.bitcoin.cz/stats/json/";
	
	public final static String WEMINELTC_ACCOUNT_URL = "https://www.wemineltc.com/api?api_key=";
	
	public final static String MT_GOX_API_URL = "http://data.mtgox.com/api/1/BTC~/ticker_fast"; // ~ is replaced with the desired currency.
	
	private static Context context;
	private static SharedPreferences prefs;
	public static String[] currencyType;
	public static String[] currencySymbol;
	public static boolean[] currencyIsPrefix;
	
	private ActivePoolView activeView = ActivePoolView.WeMineLTC;
	
	// Slush's Pool
	private String slushsAPIKey;
	private boolean slushShowingBlocks;
	private boolean slushAutoConnect;
	private int slushConnectionDelay;
	private boolean slushShowHashrateUnit;
	private boolean slushShowParseMessage;
	
	// WeMineLTC's Pool
	private String wemineltcAPIKey;
	private boolean wemineltcAutoConnect;
	private int wemineltcConnectionDelay;
	private boolean wemineltcShowHashrateUnit;
	private boolean wemineltcShowParseMessage;
	
	private boolean canCheckConnectionDelays;
	
	private boolean shouldUseBackupHttpUserAgent;
	private String httpUserAgent;
	
	private boolean mtGoxFetchEnabled;
	private int mtGoxFetchDelay;
	private String mtGoxCurrencyType;
	private boolean mtGoxEffectBlockTable;
	private boolean mtGoxBTCTOCurrencySymbolPrefix;
	private String mtGoxBTCToCurrencySymbol;
	
	public Settings(Context theContext, Resources r) {
		context = theContext;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		currencyType = r.getStringArray(R.array.supported_currencies_convertable_from_btc);
		currencySymbol = r.getStringArray(R.array.currency_type_to_symbol);
		String[] tempArray = r.getStringArray(R.array.currency_type_is_prefix);
		currencyIsPrefix = new boolean[tempArray.length];
		for(int i = 0; i < tempArray.length; i++)
			currencyIsPrefix[i] = Boolean.parseBoolean(tempArray[i]);
		tempArray = null;
	}
	
	public void load(Resources r) {
		
		// Slush's Pool
		slushsAPIKey = prefs.getString("settings_slush_api_key", "");
		slushShowingBlocks = prefs.getBoolean("showing_slush_blocks", false);
		slushAutoConnect = prefs.getBoolean("settings_slush_auto_connect", r.getBoolean(R.bool.auto_connect));
		try {
			slushConnectionDelay = Integer.parseInt(prefs.getString("settings_slush_connect_delay", r.getString(R.string.connection_delay)));
		} catch(Exception e) {
			setSlushConnectionDelay(Integer.parseInt(r.getString(R.string.connection_delay)));
			Toast.makeText(context, R.string.problem_connection_delay_invalid, Toast.LENGTH_LONG).show();
		}
		slushShowHashrateUnit = prefs.getBoolean("settings_slush_show_hashrates", r.getBoolean(R.bool.show_hashrates));
		slushShowParseMessage = prefs.getBoolean("settings_slush_show_messages_when_parsed", r.getBoolean(R.bool.show_parsing_messages));
		
		// WeMineLTC's Pool
		wemineltcAPIKey = prefs.getString("settings_wemineltc_api_key", "");
		wemineltcAutoConnect = prefs.getBoolean("settings_wemineltc_auto_connect", r.getBoolean(R.bool.auto_connect));
		try {
			wemineltcConnectionDelay = Integer.parseInt(prefs.getString("settings_wemineltc_connect_delay", r.getString(R.string.connection_delay)));
		} catch(Exception e) {
			setSlushConnectionDelay(Integer.parseInt(r.getString(R.string.connection_delay)));
			Toast.makeText(context, R.string.problem_connection_delay_invalid, Toast.LENGTH_LONG).show();
		}
		wemineltcShowHashrateUnit = prefs.getBoolean("settings_wemineltc_show_hashrates", r.getBoolean(R.bool.show_hashrates));
		wemineltcShowParseMessage = prefs.getBoolean("settings_wemineltc_show_messages_when_parsed", r.getBoolean(R.bool.show_parsing_messages));
		//
		
		canCheckConnectionDelays = prefs.getBoolean("settings_check_connection_delays", r.getBoolean(R.bool.check_connection_delays));
		shouldUseBackupHttpUserAgent = prefs.getBoolean("settings_force_use_backup_user_agent", r.getBoolean(R.bool.force_custom_user_agent));
		String userAgent = prefs.getString("settings_backup_user_agent", "");
		userAgent = userAgent.equals("") ? null : userAgent;
		httpUserAgent = shouldUseBackupHttpUserAgent ? userAgent : System.getProperty("http.agent", userAgent);
		
		mtGoxFetchEnabled = prefs.getBoolean("settings_mtgox_enabled", r.getBoolean(R.bool.mtgox_enabled));
		try {
			mtGoxFetchDelay = Integer.parseInt(prefs.getString("settings_mtgox_fetch_rate", r.getString(R.string.mtgox_currency_exchange_fetch_rate)));
		} catch(Exception e) {
			setMtGoxFetchDelay(Integer.parseInt(r.getString(R.string.mtgox_currency_exchange_fetch_rate)));
			Toast.makeText(context, R.string.problem_mtgox_connection_rate_invalid, Toast.LENGTH_LONG).show();
		}
		mtGoxCurrencyType = prefs.getString("settings_mtgox_currency_type", r.getString(R.string.mtgox_currency_type));
		mtGoxEffectBlockTable = prefs.getBoolean("settings_mtgox_effect_block_table", r.getBoolean(R.bool.mtgox_effect_block_table));
		
		mtGoxBTCTOCurrencySymbolPrefix = true; // TODO: Find out what currencies prefix, and what ones suffix.
		mtGoxBTCToCurrencySymbol = "$";
		for(int i = 0; i < currencyType.length; i++)
			if(currencyType[i].equals(mtGoxCurrencyType)) {
				mtGoxBTCToCurrencySymbol = currencySymbol[i];
				mtGoxBTCTOCurrencySymbolPrefix = currencyIsPrefix[i];
				break;
			}
	}
	
	public ActivePoolView getActiveView() {
		return activeView;
	}
	
	public void setActiveView(ActivePoolView activeView) {
		this.activeView = activeView;
	}
	
	public String getSlushsAPIKey() {
		return slushsAPIKey;
	}
	
	public boolean isSlushShowingBlocks() {
		return slushShowingBlocks;
	}
	
	public boolean canSlushAutoConnect() {
		return slushAutoConnect;
	}
	
	public int getSlushConnectionDelay() {
		return slushConnectionDelay;
	}
	
	public void setSlushConnectionDelay(int slushConnectionDelay) {
		this.slushConnectionDelay = slushConnectionDelay;
		prefs.edit().putString("settings_slush_connect_delay", Integer.toString(slushConnectionDelay)).commit();
	}
	
	public void setSlushShowingBlocks(boolean slushShowingBlocks) {
		this.slushShowingBlocks = slushShowingBlocks;
		prefs.edit().putBoolean("showing_slush_blocks", slushShowingBlocks).commit();
	}
	
	public boolean canSlushShowHashrateUnit() {
		return slushShowHashrateUnit;
	}
	
	public boolean canSlushShowParseMessage() {
		return slushShowParseMessage;
	}
	
	public String getWeMineLTCAPIKey() {
		return wemineltcAPIKey;
	}
	
	public boolean canWeMineLTCAutoConnect() {
		return wemineltcAutoConnect;
	}
	
	public int getWeMineLTCConnectionDelay() {
		return wemineltcConnectionDelay;
	}
	
	public void setWeMineLTCConnectionDelay(int wemineltcConnectionDelay) {
		this.wemineltcConnectionDelay = wemineltcConnectionDelay;
		prefs.edit().putString("settings_wemineltc_connect_delay", Integer.toString(wemineltcConnectionDelay)).commit();
	}
	
	public boolean canWeMineLTCShowHashrateUnit() {
		return wemineltcShowHashrateUnit;
	}
	
	public boolean canWeMineLTCShowParseMessage() {
		return wemineltcShowParseMessage;
	}
	
	public boolean canCheckConnectionDelays() {
		return canCheckConnectionDelays;
	}
	
	public void setCheckConnectionDelays(boolean canCheckConnectionDelays) {
		this.canCheckConnectionDelays = canCheckConnectionDelays;
		prefs.edit().putBoolean("settings_check_connection_delays", canCheckConnectionDelays).commit();
	}
	
	public boolean shouldUseBackupHttpUserAgent() {
		return shouldUseBackupHttpUserAgent;
	}
	
	public String getHTTPUserAgent() {
		return httpUserAgent;
	}
	
	public boolean isMtGoxFetchEnabled() {
		return mtGoxFetchEnabled;
	}
	
	public void setMtGoxFetchEnabled(boolean mtGoxFetchEnabled) {
		this.mtGoxFetchEnabled = mtGoxFetchEnabled;
		prefs.edit().putBoolean("settings_mtgox_enabled", mtGoxFetchEnabled).commit();
	}
	
	public int getMtGoxFetchDelay() {
		return mtGoxFetchDelay;
	}
	
	public void setMtGoxFetchDelay(int mtGoxFetchDelay) {
		this.mtGoxFetchDelay = mtGoxFetchDelay;
		prefs.edit().putString("settings_mtgox_fetch_rate", Integer.toString(mtGoxFetchDelay)).commit();
	}
	
	public String getMtGoxCurrencyType() {
		return mtGoxCurrencyType;
	}
	
	public boolean canMtGoxEffectBlockTable() {
		return mtGoxEffectBlockTable;
	}
	
	public boolean canTheMtGoxBTCTOCurrencySymbolPrefixOrAffix() {
		return mtGoxBTCTOCurrencySymbolPrefix;
	}
	
	public String getTheMtGoxBTCToCurrencySymbol() {
		return mtGoxBTCToCurrencySymbol;
	}
}
