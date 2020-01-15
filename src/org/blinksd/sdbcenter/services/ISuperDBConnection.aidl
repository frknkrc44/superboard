package org.blinksd.sdbcenter.services;

interface ISuperDBConnection {
	
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
	
	// getters for []
	String[] getStringArray(String pkgName, String dbName, String key, in String[] def);
	int[] getIntegerArray(String pkgName, String dbName, String key, in int[] def);
	long[] getLongArray(String pkgName, String dbName, String key, in long[] def);
	byte[] getByteArray(String pkgName, String dbName, String key, in byte[] def);
	float[] getFloatArray(String pkgName, String dbName, String key, in float[] def);
	double[] getDoubleArray(String pkgName, String dbName, String key, in double[] def);
	boolean[] getBooleanArray(String pkgName, String dbName, String key, in boolean[] def);
	
	// setters for []
	void putStringArray(String pkgName, String dbName, String key, in String[] value);
	void putIntegerArray(String pkgName, String dbName, String key, in int[] value);
	void putLongArray(String pkgName, String dbName, String key, in long[] value);
	void putByteArray(String pkgName, String dbName, String key, in byte[] value);
	void putFloatArray(String pkgName, String dbName, String key, in float[] value);
	void putDoubleArray(String pkgName, String dbName, String key, in double[] value);
	void putBooleanArray(String pkgName, String dbName, String key, in boolean[] value);
	
}
