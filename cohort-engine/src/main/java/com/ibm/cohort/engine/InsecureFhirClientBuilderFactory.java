/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import ca.uhn.fhir.context.FhirContext;

/**
 * An implementation of FhirClientBuilderFactory that ignores SSL CA and hostname verification.
 *
 * THIS SHOULD NOT BE USED IN PRODUCTION
 */
public class InsecureFhirClientBuilderFactory extends FhirClientBuilderFactory {

	@Override
	public FhirClientBuilder newFhirClientBuilder() {
		return newFhirClientBuilder(FhirContext.forR4());
	}

	@Override
	public FhirClientBuilder newFhirClientBuilder(FhirContext fhirContext) {
		fhirContext.setRestfulClientFactory(
				new InsecureApacheRestfulClientFactory(fhirContext)
		);

		return new DefaultFhirClientBuilder(fhirContext);
	}

}
