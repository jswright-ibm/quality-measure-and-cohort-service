/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.zip.ZipFile;

import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

import com.ibm.cohort.engine.translation.CqlTranslationProvider;
import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;

public class ZipLibrarySourceProviderTest {
	@Test
	public void testLibraryFoundInZipSuccess() throws Exception {
		ZipFile zip = new ZipFile( "src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip");
		
		ZipLibrarySourceProvider provider = new ZipLibrarySourceProvider(zip);
		try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("FHIRHelpers") ) ) { 
			assertNotNull( is );
			
			CqlTranslationProvider tx = new InJVMCqlTranslationProvider();
			Library library = tx.translate( is );
			assertEquals( "FHIRHelpers", library.getIdentifier().getId() );
		}
	}
}
