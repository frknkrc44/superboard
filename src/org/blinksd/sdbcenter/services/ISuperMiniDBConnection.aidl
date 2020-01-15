package org.blinksd.sdbcenter.services;

interface ISuperMiniDBConnection {

	// database connection
	void connect(String pkgName, String dbName);
	void disconnect(String pkgName, String dbName);
	
	// sync
	Map exportDatabase(String pkgName, String dbName);
	void importDatabase(String pkgName, String dbName, in Map values);
	void syncDatabase(String pkgName, String dbName, inout Map values);
	void removeDatabase(String pkgName, String dbName);
	
	// others
	void removeKeyFromDB(String pkgName, String dbName, String key);
	boolean isDBContainsKey(String pkgName, String dbName, String key);
	
	// getters
	String getString(String pkgName, String dbName, String key, String def);
	int getInteger(String pkgName, String dbName, String key, int def);
	long getLong(String pkgName, String dbName, String key, long def);
	byte getByte(String pkgName, String dbName, String key, byte def);
	float getFloat(String pkgName, String dbName, String key, float def);
	double getDouble(String pkgName, String dbName, String key, double def);
	boolean getBoolean(String pkgName, String dbName, String key, boolean def);
	
	// setters
	void putString(String pkgName, String dbName, String key, String value);
	void putInteger(String pkgName, String dbName, String key, int value);
	void putLong(String pkgName, String dbName, String key, long value);
	void putByte(String pkgName, String dbName, String key, byte value);
	void putFloat(String pkgName, String dbName, String key, float value);
	void putDouble(String pkgName, String dbName, String key, double value);
	void putBoolean(String pkgName, String dbName, String key, boolean value);
	
}
