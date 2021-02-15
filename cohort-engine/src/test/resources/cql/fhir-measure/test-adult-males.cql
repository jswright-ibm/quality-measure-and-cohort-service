library "TestAdultMales" version '1.0.0'
using "FHIR" version '4.0.0'

include "TestAdultMales2" version '1.0.0'
include "TestAdultMales3" version '1.0.0'

parameter MeasurementPeriod Interval<DateTime>

context Patient

define "Initial Population":
	"TestAdultMales2"."Over 18"
	
define "Denominator":
	"Initial Population"
	
define "Numerator":
	"TestAdultMales3".Male