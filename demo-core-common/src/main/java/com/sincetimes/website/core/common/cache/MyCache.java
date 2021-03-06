package com.sincetimes.website.core.common.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.sincetimes.website.core.common.support.LogCore;

/**
 * 单机缓存
 */
public class MyCache<K, V> {
	private Cache<K, V> cache = null;

	/**
	 * @param expire_time
	 *            过期时间,单位为天
	 */
	public MyCache(int max_size, int expire_time) {
		cache = CacheBuilder.newBuilder().recordStats().maximumSize(max_size)
				.expireAfterWrite(expire_time, TimeUnit.DAYS).build();
	}

	/** 如果缓存和数据库中没有或者存的为空值则返回 false **/
	public boolean containsKey(K key, Function<K, V> fnc) {
		return null != getValue(key, fnc);
	}

	/***
	 * 执行的查找函数,此函数可以是从持久化或者网路取得
	 * 允许返回空
	 * @return nullable
	 */
	public V getValue(K key, Function<K, V> fnc_get) {
		try {
			return cache.get(key, () -> fnc_get.apply(key));
		} catch (Exception e) {
			LogCore.BASE.warn("cache get value null, need persistance in other ways,key= {}",key);
			return null;
		}
	}

	public V putValue(K key, V value, BiConsumer<K, V> func_put) {
		func_put.accept(key, value);
		return cache.asMap().put(key, value);
	}

	public long size() {
		return cache.size();
	}

	public CacheStats getStats() {
		return cache.stats();
	}
}
