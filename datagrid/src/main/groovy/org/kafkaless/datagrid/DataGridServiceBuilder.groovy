package org.kafkaless.datagrid

import static com.google.common.io.Files.createTempDir

class DataGridServiceBuilder {

    DefaultDataGridService build() {
        new DefaultDataGridService(createTempDir())
    }

}
