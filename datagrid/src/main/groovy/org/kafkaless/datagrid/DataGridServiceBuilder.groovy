package org.kafkaless.datagrid

import static com.google.common.io.Files.createTempDir

class DataGridServiceBuilder {

    DataGridService build() {
        new DataGridService(createTempDir())
    }

}
