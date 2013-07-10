package net.nexustools.jbitminingstats.util;

import net.nexustools.jbitminingstats.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Settings {
	private static Context context;
	private static SharedPreferences prefs;
	public static String[] currencyType;
	public static String[] currencySymbol;
	public static boolean[] currencyIsPrefix;
	
	private boolean showingBlocks;
	
	private boolean autoConnect;
	private int connectionDelay;
	private boolean showHashrateUnit;
	private boolean showParseMessage;
	private boolean canCheckConnectionDelays;
	
	private boolean shouldUseBackupHttpUserAgent;
	private String httpUserAgent;
	
	private String slushsAccountDomain;
	private String slushsBlockDomain;
	private String slushsAPIKey;
	
	private boolean mtGoxFetchEnabled;
	private int mtGoxFetchDelay;
	private String mtGoxAPIDomain;
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
		showingBlocks = prefs.getBoolean("showing_blocks", false);
		autoConnect = prefs.getBoolean("settings_auto_connect", r.getBoolean(R.bool.auto_connect));
		try {
			connectionDelay = Integer.parseInt(prefs.getString("settings_connect_delay", r.getString(R.string.connection_delay)));
		} catch(Exception e) {
			setConnectionDelay(Integer.parseInt(r.getString(R.string.connection_delay)));
			Toast.makeText(context, R.string.problem_connection_delay_invalid, Toast.LENGTH_LONG).show();
		}
		showHashrateUnit = prefs.getBoolean("settings_show_hashrates", r.getBoolean(R.bool.show_hashrates));
		showParseMessage = prefs.getBoolean("settings_show_messages_when_parsed", r.getBoolean(R.bool.show_parsing_messages));
		canCheckConnectionDelays = prefs.getBoolean("settings_check_connection_delays", r.getBoolean(R.bool.check_connection_delays));
		
		shouldUseBackupHttpUserAgent = prefs.getBoolean("settings_force_use_backup_user_agent", r.getBoolean(R.bool.force_custom_user_agent));
		String userAgent = prefs.getString("settings_backup_user_agent", "");
		userAgent = userAgent.equals("") ? null : userAgent;
		httpUserAgent = shouldUseBackupHttpUserAgent ? userAgent : System.getProperty("http.agent", userAgent);
		
		slushsAccountDomain = prefs.getString("settings_slushs_account_api_domain", r.getString(R.string.slushs_block_domain));
		slushsBlockDomain = prefs.getString("settings_slushs_block_api_domain", r.getString(R.string.slushs_miner_domain));
		slushsAPIKey = prefs.getString("settings_slushs_api_key", "");
		
		mtGoxFetchEnabled = prefs.getBoolean("settings_mtgox_enabled", r.getBoolean(R.bool.mtgox_enabled));
		try {
			mtGoxFetchDelay = Integer.parseInt(prefs.getString("settings_mtgox_fetch_rate", r.getString(R.string.mtgox_currency_exchange_fetch_rate)));
		} catch(Exception e) {
			setMtGoxFetchDelay(Integer.parseInt(r.getString(R.string.mtgox_currency_exchange_fetch_rate)));
			Toast.makeText(context, R.string.problem_mtgox_connection_rate_invalid, Toast.LENGTH_LONG).show();
		}
		mtGoxAPIDomain = prefs.getString("settings_mtgox_api_domain", r.getString(R.string.mtgox_api_domain));
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
	
	public boolean isShowingBlocks() {
		return showingBlocks;
	}
	
	public boolean canAutoConnect() {
		return autoConnect;
	}
	
	public int getConnectionDelay() {
		return connectionDelay;
	}
	
	public void setConnectionDelay(int connectionDelay) {
		this.connectionDelay = connectionDelay;
		prefs.edit().putString("settings_connect_delay", Integer.toString(connectionDelay)).commit();
	}
	
	public void setShowingBlocks(boolean showingBlocks) {
		this.showingBlocks = showingBlocks;
		prefs.edit().putBoolean("showing_blocks", showingBlocks).commit();
	}
	
	public boolean canShowHashrateUnit() {
		return showHashrateUnit;
	}
	
	public boolean canShowParseMessage() {
		return showParseMessage;
	}
	
	public boolean canCheckConnectionDelays() {
		return canCheckConnectionDelays;
	}
	
	public void setCheckConnectionDelays(boolean canCheckConnectionDelays) {
		this.canCheckConnectionDelays = canCheckConnectionDelays;
		prefs.edit().putBoolean("settings_check_connection_delays", canCheckConnectionDelays).commit();
	}
	
	public String getSlushsAccountDomain() {
		return slushsAccountDomain;
	}
	
	public String getSlushsBlockDomain() {
		return slushsBlockDomain;
	}
	
	public String getSlushsAPIKey() {
		return slushsAPIKey;
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
	
	public String getMtGoxAPIDomain() {
		return mtGoxAPIDomain;
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
