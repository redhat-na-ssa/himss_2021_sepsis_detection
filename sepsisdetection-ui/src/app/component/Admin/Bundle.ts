import { Injectable } from '@angular/core';
@Injectable({ providedIn: 'root' })
export class Bundle  {

    configuration : any;

    public getConfiguration() : any
    {
        return this.configuration;
    }

    constructor()
     {
         this.configuration = {
            "resourceType": "Bundle",
            "id": "c5aea91d-21ee-4a6c-bf13-c59ec39995e4",
            "meta": {
                "lastUpdated": "2021-07-15T22:31:47.289+00:00"
            },
            "type": "transaction",
            "total": 11,
            "link": [
                {
                    "relation": "self",
                    "url": "http://localhost:8000/Observation?_pretty=true"
                }
            ],
            "entry": [
                {
                    "request": {
                        "method": "POST",
                        "url": "Observation"
                    },
                    "resource": {
                        "resourceType": "Observation",
                        "id": "satO2",
                        "meta": {
                            "profile": [
                                "http://hl7.org/fhir/StructureDefinition/vitalsigns"
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: satO2</p><p><b>meta</b>: </p><p><b>identifier</b>: o1223435-10</p><p><b>partOf</b>: <a>Procedure/ob</a></p><p><b>status</b>: final</p><p><b>category</b>: Vital Signs <span>(Details : {http://terminology.hl7.org/CodeSystem/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b>: Oxygen saturation in Arterial blood <span>(Details : {LOINC code '2708-6' = 'Oxygen saturation in Arterial blood', given as 'Oxygen saturation in Arterial blood'}; {LOINC code '59408-5' = 'Oxygen saturation in Arterial blood by Pulse oximetry', given as 'Oxygen saturation in Arterial blood by Pulse oximetry'}; {urn:iso:std:iso:11073:10101 code '150456' = '150456', given as 'MDC_PULS_OXIM_SAT_O2'})</span></p><p><b>subject</b>: <a>Patient/1</a></p><p><b>effective</b>: 05/12/2014 9:30:10 AM</p><p><b>value</b>: 95 %<span> (Details: UCUM code % = '%')</span></p><p><b>interpretation</b>: Normal (applies to non-numeric results) <span>(Details : {http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation code 'N' = 'Normal', given as 'Normal'})</span></p><p><b>device</b>: <a>DeviceMetric/example</a></p><h3>ReferenceRanges</h3><table><tr><td>-</td><td><b>Low</b></td><td><b>High</b></td></tr><tr><td>*</td><td>90 %<span> (Details: UCUM code % = '%')</span></td><td>99 %<span> (Details: UCUM code % = '%')</span></td></tr></table></div>"
                        },
                        "identifier": [
                            {
                                "system": "http://goodcare.org/observation/id",
                                "value": "o1223435-10"
                            }
                        ],
                        "status": "final",
                        "category": [
                            {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/observation-category",
                                        "code": "vital-signs",
                                        "display": "Vital Signs"
                                    }
                                ],
                                "text": "Vital Signs"
                            }
                        ],
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "2708-6",
                                    "display": "Oxygen saturation in Arterial blood"
                                },
                                {
                                    "system": "http://loinc.org",
                                    "code": "59408-5",
                                    "display": "Oxygen saturation in Arterial blood by Pulse oximetry"
                                },
                                {
                                    "system": "urn:iso:std:iso:11073:10101",
                                    "code": "150456",
                                    "display": "MDC_PULS_OXIM_SAT_O2"
                                }
                            ]
                        },
                        "subject": {
                            "reference": "Patient/1"
                        },
                        "effectiveDateTime": "2014-12-05T09:30:10+01:00",
                        "valueQuantity": {
                            "value": 95,
                            "unit": "%",
                            "system": "http://unitsofmeasure.org",
                            "code": "%"
                        },
                        "interpretation": [
                            {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                                        "code": "N",
                                        "display": "Normal"
                                    }
                                ],
                                "text": "Normal (applies to non-numeric results)"
                            }
                        ],
                        "referenceRange": [
                            {
                                "low": {
                                    "value": 90,
                                    "unit": "%",
                                    "system": "http://unitsofmeasure.org",
                                    "code": "%"
                                },
                                "high": {
                                    "value": 99,
                                    "unit": "%",
                                    "system": "http://unitsofmeasure.org",
                                    "code": "%"
                                }
                            }
                        ]
                    }
                },
                {
                    "request": {
                        "method": "POST",
                        "url": "Observation"
                    },
                    "resource": {
                        "resourceType": "Observation",
                        "id": "respiratory-rate",
                        "meta": {
                            "profile": [
                                "http://hl7.org/fhir/StructureDefinition/vitalsigns"
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: respiratory-rate</p><p><b>meta</b>: </p><p><b>status</b>: final</p><p><b>category</b>: Vital Signs <span>(Details : {http://terminology.hl7.org/CodeSystem/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b>: Respiratory rate <span>(Details : {LOINC code '9279-1' = 'Respiratory rate', given as 'Respiratory rate'})</span></p><p><b>subject</b>: <a>Patient/1</a></p><p><b>effective</b>: 02/07/1999</p><p><b>value</b>: 26 breaths/minute<span> (Details: UCUM code /min = '/min')</span></p></div>"
                        },
                        "status": "final",
                        "category": [
                            {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/observation-category",
                                        "code": "vital-signs",
                                        "display": "Vital Signs"
                                    }
                                ],
                                "text": "Vital Signs"
                            }
                        ],
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "9279-1",
                                    "display": "Respiratory rate"
                                }
                            ],
                            "text": "Respiratory rate"
                        },
                        "subject": {
                            "reference": "Patient/1"
                        },
                        "effectiveDateTime": "1999-07-02",
                        "valueQuantity": {
                            "value": 26,
                            "unit": "breaths/minute",
                            "system": "http://unitsofmeasure.org",
                            "code": "/min"
                        }
                    }
                },
                {
                    "request": {
                        "method": "POST",
                        "url": "Observation"
                    },
                    "resource": {
                        "resourceType": "Observation",
                        "id": "blood-pressure",
                        "meta": {
                            "profile": [
                                "http://hl7.org/fhir/StructureDefinition/vitalsigns"
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: blood-pressure</p><p><b>meta</b>: </p><p><b>identifier</b>: urn:uuid:187e0c12-8dd2-67e2-99b2-bf273c878281</p><p><b>basedOn</b>: </p><p><b>status</b>: final</p><p><b>category</b>: Vital Signs <span>(Details : {http://terminology.hl7.org/CodeSystem/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b>: Blood pressure systolic &amp; diastolic <span>(Details : {LOINC code '85354-9' = 'Blood pressure panel with all children optional', given as 'Blood pressure panel with all children optional'})</span></p><p><b>subject</b>: <a>Patient/1</a></p><p><b>effective</b>: 17/09/2012</p><p><b>performer</b>: <a>Practitioner/example</a></p><p><b>interpretation</b>: Below low normal <span>(Details : {http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation code 'L' = 'Low', given as 'low'})</span></p><p><b>bodySite</b>: Right arm <span>(Details : {SNOMED CT code '368209003' = 'Right upper arm', given as 'Right arm'})</span></p><blockquote><p><b>component</b></p><p><b>code</b>: Systolic blood pressure <span>(Details : {LOINC code '8480-6' = 'Systolic blood pressure', given as 'Systolic blood pressure'}; {SNOMED CT code '271649006' = 'Systolic blood pressure', given as 'Systolic blood pressure'}; {http://acme.org/devices/clinical-codes code 'bp-s' = 'bp-s', given as 'Systolic Blood pressure'})</span></p><p><b>value</b>: 107 mmHg<span> (Details: UCUM code mm[Hg] = 'mmHg')</span></p><p><b>interpretation</b>: Normal <span>(Details : {http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation code 'N' = 'Normal', given as 'normal'})</span></p></blockquote><blockquote><p><b>component</b></p><p><b>code</b>: Diastolic blood pressure <span>(Details : {LOINC code '8462-4' = 'Diastolic blood pressure', given as 'Diastolic blood pressure'})</span></p><p><b>value</b>: 60 mmHg<span> (Details: UCUM code mm[Hg] = 'mmHg')</span></p><p><b>interpretation</b>: Below low normal <span>(Details : {http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation code 'L' = 'Low', given as 'low'})</span></p></blockquote></div>"
                        },
                        "identifier": [
                            {
                                "system": "urn:ietf:rfc:3986",
                                "value": "urn:uuid:187e0c12-8dd2-67e2-99b2-bf273c878281"
                            }
                        ],
                        "basedOn": [
                            {
                                "identifier": {
                                    "system": "https://acme.org/identifiers",
                                    "value": "1234"
                                }
                            }
                        ],
                        "status": "final",
                        "category": [
                            {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/observation-category",
                                        "code": "vital-signs",
                                        "display": "Vital Signs"
                                    }
                                ]
                            }
                        ],
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "85354-9",
                                    "display": "Blood pressure panel with all children optional"
                                }
                            ],
                            "text": "Blood pressure systolic & diastolic"
                        },
                        "subject": {
                            "reference": "Patient/1"
                        },
                        "effectiveDateTime": "2012-09-17",
                        "interpretation": [
                            {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                                        "code": "L",
                                        "display": "low"
                                    }
                                ],
                                "text": "Below low normal"
                            }
                        ],
                        "bodySite": {
                            "coding": [
                                {
                                    "system": "http://snomed.info/sct",
                                    "code": "368209003",
                                    "display": "Right arm"
                                }
                            ]
                        },
                        "component": [
                            {
                                "code": {
                                    "coding": [
                                        {
                                            "system": "http://loinc.org",
                                            "code": "8480-6",
                                            "display": "Systolic blood pressure"
                                        },
                                        {
                                            "system": "http://snomed.info/sct",
                                            "code": "271649006",
                                            "display": "Systolic blood pressure"
                                        },
                                        {
                                            "system": "http://acme.org/devices/clinical-codes",
                                            "code": "bp-s",
                                            "display": "Systolic Blood pressure"
                                        }
                                    ]
                                },
                                "valueQuantity": {
                                    "value": 107,
                                    "unit": "mmHg",
                                    "system": "http://unitsofmeasure.org",
                                    "code": "mm[Hg]"
                                },
                                "interpretation": [
                                    {
                                        "coding": [
                                            {
                                                "system": "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                                                "code": "N",
                                                "display": "normal"
                                            }
                                        ],
                                        "text": "Normal"
                                    }
                                ]
                            },
                            {
                                "code": {
                                    "coding": [
                                        {
                                            "system": "http://loinc.org",
                                            "code": "8462-4",
                                            "display": "Diastolic blood pressure"
                                        }
                                    ]
                                },
                                "valueQuantity": {
                                    "value": 60,
                                    "unit": "mmHg",
                                    "system": "http://unitsofmeasure.org",
                                    "code": "mm[Hg]"
                                },
                                "interpretation": [
                                    {
                                        "coding": [
                                            {
                                                "system": "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                                                "code": "L",
                                                "display": "low"
                                            }
                                        ],
                                        "text": "Below low normal"
                                    }
                                ]
                            }
                        ]
                    }
                },
                {
                    "request": {
                        "method": "POST",
                        "url": "Observation"
                    },
                    "resource": {
                        "resourceType": "Observation",
                        "id": "heart-rate",
                        "meta": {
                            "profile": [
                                "http://hl7.org/fhir/StructureDefinition/vitalsigns"
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: heart-rate</p><p><b>meta</b>: </p><p><b>status</b>: final</p><p><b>category</b>: Vital Signs <span>(Details : {http://terminology.hl7.org/CodeSystem/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b>: Heart rate <span>(Details : {LOINC code '8867-4' = 'Heart rate', given as 'Heart rate'})</span></p><p><b>subject</b>: <a>Patient/1</a></p><p><b>effective</b>: 02/07/1999</p><p><b>value</b>: 44 beats/minute<span> (Details: UCUM code /min = '/min')</span></p></div>"
                        },
                        "status": "final",
                        "category": [
                            {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/observation-category",
                                        "code": "vital-signs",
                                        "display": "Vital Signs"
                                    }
                                ],
                                "text": "Vital Signs"
                            }
                        ],
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "8867-4",
                                    "display": "Heart rate"
                                }
                            ],
                            "text": "Heart rate"
                        },
                        "subject": {
                            "reference": "Patient/1"
                        },
                        "effectiveDateTime": "1999-07-02",
                        "valueQuantity": {
                            "value": 44,
                            "unit": "beats/minute",
                            "system": "http://unitsofmeasure.org",
                            "code": "/min"
                        }
                    }
                },
                {
                    "request": {
                        "method": "POST",
                        "url": "Observation"
                    },
                    "resource": {
                        "request": {
                            "method": "POST",
                            "url": "Observation"
                        },
                        "resourceType": "Observation",
                        "id": "body-temperature",
                        "meta": {
                            "profile": [
                                "http://hl7.org/fhir/StructureDefinition/vitalsigns"
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: body-temperature</p><p><b>meta</b>: </p><p><b>status</b>: final</p><p><b>category</b>: Vital Signs <span>(Details : {http://terminology.hl7.org/CodeSystem/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b>: Body temperature <span>(Details : {LOINC code '8310-5' = 'Body temperature', given as 'Body temperature'})</span></p><p><b>subject</b>: <a>Patient/1</a></p><p><b>effective</b>: 02/07/1999</p><p><b>value</b>: 36.5 C<span> (Details: UCUM code Cel = 'Cel')</span></p></div>"
                        },
                        "status": "final",
                        "category": [
                            {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/observation-category",
                                        "code": "vital-signs",
                                        "display": "Vital Signs"
                                    }
                                ],
                                "text": "Vital Signs"
                            }
                        ],
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "8310-5",
                                    "display": "Body temperature"
                                }
                            ],
                            "text": "Body temperature"
                        },
                        "subject": {
                            "reference": "Patient/1"
                        },
                        "effectiveDateTime": "1999-07-02",
                        "valueQuantity": {
                            "value": 36.5,
                            "unit": "C",
                            "system": "http://unitsofmeasure.org",
                            "code": "Cel"
                        }
                    }
                },
                {
                    "request": {
                        "method": "POST",
                        "url": "Observation"
                    },
                    "fullUrl": "http://localhost:8000/Observation/104",
                    "resource": {
                        "resourceType": "Observation",
                        "id": "104",
                        "meta": {
                            "versionId": "17",
                            "lastUpdated": "2021-07-15T19:57:05.455+00:00",
                            "source": "#qEmOhrXiE25ZM4cq"
                        },
                        "status": "final",
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "35200-5",
                                    "display": "Cholesterol [Moles/â€‹volume] in Serum or Plasma"
                                }
                            ],
                            "text": "Cholesterol"
                        },
                        "subject": {
                            "reference": "Patient/1"
                        },
                        "performer": [
                            {
                                "system": "http://michigan.gov/state-dept-ids",
                                "value": "24"
                            }
                        ],
                        "valueQuantity": {
                            "value": 5.7,
                            "unit": "mmol/L",
                            "system": "http://unitsofmeasure.org",
                            "code": "mmol/L"
                        },
                        "referenceRange": [
                            {
                                "low": {
                                    "value": 4.5,
                                    "unit": "mmol/L",
                                    "system": "http://unitsofmeasure.org",
                                    "code": "mmol/L"
                                }
                            }
                        ]
                    },
                    "search": {
                        "mode": "match"
                    }
                },
                {
                    "request": {
                        "method": "POST",
                        "url": "Patient"
                    },
                    "fullUrl": "http://localhost:8000/Patient/1",
                    "resource": {
                        "resourceType": "Patient",
                        "id": "1",
                        "meta": {
                            "versionId": "1",
                            "lastUpdated": "2021-07-09T20:51:25.347+00:00",
                            "source": "#JpXqaV7C4xAHMuRD"
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\">\n\t\t\t<table>\n\t\t\t\t<tbody>\n\t\t\t\t\t<tr>\n\t\t\t\t\t\t<td>Name</td>\n\t\t\t\t\t\t<td>Alani Lee \n              <b>Chalmers</b> (&quot;Jim&quot;)\n            </td>\n\t\t\t\t\t</tr>\n\t\t\t\t\t<tr>\n\t\t\t\t\t\t<td>Address</td>\n\t\t\t\t\t\t<td>534 Erewhon, Pleasantville, Vic, 3999</td>\n\t\t\t\t\t</tr>\n\t\t\t\t\t<tr>\n\t\t\t\t\t\t<td>Contacts</td>\n\t\t\t\t\t\t<td>Home: unknown. Work: (03) 5555 6473</td>\n\t\t\t\t\t</tr>\n\t\t\t\t\t<tr>\n\t\t\t\t\t\t<td>Id</td>\n\t\t\t\t\t\t<td>MRN: 12345 (Acme Healthcare)</td>\n\t\t\t\t\t</tr>\n\t\t\t\t</tbody>\n\t\t\t</table>\n\t\t</div>"
                        },
                        "identifier": [
                            {
                                "use": "usual",
                                "type": {
                                    "coding": [
                                        {
                                            "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                                            "code": "MR"
                                        }
                                    ]
                                },
                                "system": "urn:oid:1.2.36.146.595.217.0.1",
                                "value": "12345",
                                "period": {
                                    "start": "2001-05-06"
                                },
                                "assigner": {
                                    "display": "Acme Healthcare"
                                }
                            }
                        ],
                        "active": true,
                        "name": [
                            {
                                "use": "official",
                                "family": "Lee",
                                "given": [
                                    "Alani",
                                    "Lee"
                                ]
                            },
                            {
                                "use": "usual",
                                "given": [
                                    "Alani"
                                ]
                            },
                            {
                                "use": "maiden",
                                "family": "Windsor",
                                "given": [
                                    "Alani",
                                    "Lee"
                                ],
                                "period": {
                                    "end": "2002"
                                }
                            }
                        ],
                        "telecom": [
                            {
                                "use": "home"
                            },
                            {
                                "system": "phone",
                                "value": "(03) 5555 6473",
                                "use": "work",
                                "rank": 1
                            },
                            {
                                "system": "phone",
                                "value": "(03) 3410 5613",
                                "use": "mobile",
                                "rank": 2
                            },
                            {
                                "system": "phone",
                                "value": "(03) 5555 8834",
                                "use": "old",
                                "period": {
                                    "end": "2014"
                                }
                            }
                        ],
                        "gender": "male",
                        "birthDate": "1974-12-25",
                        "_birthDate": {
                            "extension": [
                                {
                                    "url": "http://hl7.org/fhir/StructureDefinition/patient-birthTime",
                                    "valueDateTime": "1974-12-25T14:35:45-05:00"
                                }
                            ]
                        },
                        "deceasedBoolean": false,
                        "address": [
                            {
                                "use": "home",
                                "type": "both",
                                "text": "534 Erewhon St PeasantVille, Rainbow, Vic  3999",
                                "line": [
                                    "534 Erewhon St"
                                ],
                                "city": "PleasantVille",
                                "district": "Rainbow",
                                "state": "Vic",
                                "postalCode": "3999",
                                "period": {
                                    "start": "1974-12-25"
                                }
                            }
                        ],
                        "contact": [
                            {
                                "relationship": [
                                    {
                                        "coding": [
                                            {
                                                "system": "http://terminology.hl7.org/CodeSystem/v2-0131",
                                                "code": "N"
                                            }
                                        ]
                                    }
                                ],
                                "name": {
                                    "family": "du Marché",
                                    "_family": {
                                        "extension": [
                                            {
                                                "url": "http://hl7.org/fhir/StructureDefinition/humanname-own-prefix",
                                                "valueString": "VV"
                                            }
                                        ]
                                    },
                                    "given": [
                                        "Bénédicte"
                                    ]
                                },
                                "telecom": [
                                    {
                                        "system": "phone",
                                        "value": "+33 (237) 998327"
                                    }
                                ],
                                "address": {
                                    "use": "home",
                                    "type": "both",
                                    "line": [
                                        "534 Erewhon St"
                                    ],
                                    "city": "PleasantVille",
                                    "district": "Rainbow",
                                    "state": "Vic",
                                    "postalCode": "3999",
                                    "period": {
                                        "start": "1974-12-25"
                                    }
                                },
                                "gender": "female",
                                "period": {
                                    "start": "2012"
                                }
                            }
                        ]
                    },
                    "search": {
                        "mode": "match"
                    }
                }
            ]
        }
  }

}


