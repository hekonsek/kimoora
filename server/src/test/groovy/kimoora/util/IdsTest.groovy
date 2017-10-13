/**
 * Licensed to the Kafkaless under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kimoora.util

import org.junit.Test

import static java.util.UUID.fromString
import static kimoora.util.Ids.randomStringId
import static kimoora.util.Ids.uuid
import static org.assertj.core.api.Assertions.assertThat

class IdsTest {

    // Tests

    @Test
    void shouldGenerateUuidString() {
        // Given
        def uuidString = uuid()

        // When
        def parsedUuid = fromString(uuidString)

        // Then
        assertThat(uuidString).isEqualTo(parsedUuid.toString())
    }

    @Test
    void shouldGenerateStringIdWith10characters() {
        // When
        def id = randomStringId()

        // Then
        assertThat(id).hasSize(10)
    }

    @Test
    void shouldGenerateStringIdWithLowerCaseCharacters() {
        // When
        def id = randomStringId()

        // Then
        assertThat(id).isEqualTo(id.toLowerCase())
    }

}
