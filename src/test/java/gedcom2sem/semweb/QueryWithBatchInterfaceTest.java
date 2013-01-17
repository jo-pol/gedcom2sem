package gedcom2sem.semweb;

import org.junit.Test;

public class QueryWithBatchInterfaceTest extends AstractQueryTest
{

    public QueryWithBatchInterfaceTest(final Boolean mashup, final Integer expectedNrOfLines, final String queryFileName)
    {
        super(mashup, expectedNrOfLines, queryFileName);
    }

    @Test
    public void run() throws Exception
    {
        String qualifiedQueryFileName = queryFileName;
        if (mashup)
            Select.main(GEDCOM_TTL, MASHUP_TTL, REPORT_TXT, qualifiedQueryFileName);
        else
            Select.main(GEDCOM_TTL, REPORT_TXT, qualifiedQueryFileName);
    }
}
