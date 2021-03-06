/*
 * Copyright 2019 Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.autostyle.serialization

import com.github.autostyle.closable.use
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

inline fun <reified T> File.deserialize(): T =
    inputStream().buffered().use({ ObjectInputStream(it) }) {
        it.readObject() as T
    }

inline fun <reified T> File.serialize(value: T) =
    outputStream().buffered().use({ ObjectOutputStream(it) }) {
        it.writeObject(value)
    }
