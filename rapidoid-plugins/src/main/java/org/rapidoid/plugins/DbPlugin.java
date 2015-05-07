package org.rapidoid.plugins;

/*
 * #%L
 * rapidoid-plugins
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.rapidoid.lambda.Callback;
import org.rapidoid.lambda.Operation;
import org.rapidoid.lambda.Predicate;

/**
 * @author Nikolche Mihajlovski
 * @since 3.0.0
 */
public interface DbPlugin {

	long insert(Object entity);

	void update(Object entity);

	void update(long id, Object entity);

	long persist(Object record);

	long insertOrGetId(Object record);

	<T> T get(long id);

	<T> T get(long id, Class<T> clazz);

	<T> T getIfExists(long id);

	<T> List<T> getAll(Class<T> clazz);

	<E> List<E> getAll(long... ids);

	<E> List<E> getAll(Iterable<Long> ids);

	long refresh(Object entity);

	void delete(long id);

	void delete(Object entity);

	<T> List<T> find(String query);

	<T> List<T> find(Class<T> clazz, Predicate<T> match, Comparator<T> orderBy);

	<E> List<E> find(Predicate<E> match);

	long size();

	<E> E entity(Class<E> entityType, Map<String, ?> properties);

	<E> List<E> query(Class<E> clazz, String query, Object... args);

	<RESULT> RESULT sql(String sql, Object... args);

	<E> void each(Operation<E> lambda);

	void transaction(Runnable transaction, boolean readOnly);

	void transaction(Runnable tx, boolean readonly, Callback<Void> callback);

	/**
	 * WARNING: Deletes ALL data in the database! Use with care!
	 */
	void deleteAllData();

}
