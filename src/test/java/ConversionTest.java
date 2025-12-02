/*
 * Copyright 2011-2025 Lime Mojito Pty Ltd
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

import com.limemojito.xero.nabtrade.CSVConverter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConversionTest {

    private final FileSandpit fileSandpit = new FileSandpit();

    @Test
    public void shouldConvertSingleFile() throws Exception {
        String statementFile = "/Cash Account Transactions.csv";
        File sandpit = fileSandpit.prepareWithResources(statementFile);

        CSVConverter converter = new CSVConverter(sandpit);
        converter.convert();

        assertAgainstExpected(statementFile);
    }

    @Test
    public void shouldLoadMultipleFiles() throws Exception {
        String statementFile = "/Cash Account Transactions.csv";
        String statementFile2 = "/Cash Account Transactions-2.csv";
        File sandpit = fileSandpit.prepareWithResources(statementFile, statementFile2);

        CSVConverter converter = new CSVConverter(sandpit);
        converter.convert();

        assertAgainstExpected(statementFile);
        assertAgainstExpected(statementFile2);
    }

    private void assertAgainstExpected(String statementFile) throws IOException {
        List<String> data = fileSandpit.loadFileContentsAsTextLines(statementFile);
        assertThat(data).isEqualTo(loadExpected(statementFile));
    }

    private List<String> loadExpected(String statementFile) throws IOException {
        return fileSandpit.loadResourceAsTextLines("/expected" + statementFile);
    }
}
