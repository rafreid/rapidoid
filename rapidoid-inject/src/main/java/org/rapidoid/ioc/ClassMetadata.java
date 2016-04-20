package org.rapidoid.ioc;

import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.annotation.Wired;
import org.rapidoid.cls.Cls;
import org.rapidoid.util.Msc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/*
 * #%L
 * rapidoid-inject
 * %%
 * Copyright (C) 2014 - 2016 Nikolche Mihajlovski and contributors
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

@Authors("Nikolche Mihajlovski")
@Since("5.1.0")
public class ClassMetadata extends RapidoidThing {

	final Class<?> clazz;

	final List<Field> injectableFields;

	public ClassMetadata(Class<?> clazz) {
		this.clazz = clazz;
		this.injectableFields = Cls.getFieldsAnnotated(clazz, Wired.class);

		if (Msc.hasInject()) {
			Class<Annotation> javaxInject = Cls.get("javax.inject.Inject");
			this.injectableFields.addAll(Cls.getFieldsAnnotated(clazz, javaxInject));
		}
	}

}
