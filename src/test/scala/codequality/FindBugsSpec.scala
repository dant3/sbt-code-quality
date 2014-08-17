package codequality

import org.specs2.mutable._

class FindBugsSpec extends Specification {
    val bugs = FindBugs.Bug.parseBugs(FindBugsSpec.sampleXml)

    "FindBugs parser with sampleXml parse output" should {
        "contain only one bug instance" in {
            bugs.size must_== 1
        }
        "contain sample bug that" should {
            val sampleBug = bugs.head
            
            s"have its category set to ${FindBugsSpec.sampleBugCategory}" in {
                sampleBug.category must_== FindBugsSpec.sampleBugCategory
            }
            s"have its type set to ${FindBugsSpec.sampleBugType}" in {
                sampleBug.`type` must_== FindBugsSpec.sampleBugType
            }
            s"have its description contain 5 lines with arrow as last one" in {
                sampleBug.detailedDescription.lines.size must_== 5 and {
                    sampleBug.detailedDescription.lines.toList(4) must startWith("+->")
                }
            }
            s"have its label set to '${FindBugsSpec.sampleBugLabel}'" in {
                sampleBug.label must_== FindBugsSpec.sampleBugLabel
            }
            s"have its at set to '${FindBugsSpec.sampleBugAt}'" in {
                sampleBug.at must_== FindBugsSpec.sampleBugAt
            }
        }
    }
}

object FindBugsSpec {
    val sampleBugCategory = "PERFORMANCE"
    val sampleBugType = "DM_BOXED_PRIMITIVE_FOR_PARSING"
    val sampleBugLabel = "Boxing/unboxing to parse a primitive"
    val sampleBugAt = "At Test.java:[line 7]"
    
    /* SAMPLE XML: */
    lazy val sampleXml =
        <BugCollection version="3.0.0" sequence="0" timestamp="1408110830000" analysisTimestamp="1408110833142" release="">
            <Project projectName="">
                <Jar>/private/var/folders/4v/x65l44qn0wb4p99c46ccx0q00000gn/T/sbt_b6f64f95/fail-on-violations/target/scala-2.10/classes/Test.class</Jar>
            </Project>
            <BugInstance type="DM_BOXED_PRIMITIVE_FOR_PARSING" priority="1" rank="16" abbrev="Bx" category="PERFORMANCE" instanceHash="9f15bc76d1c76c785eeaa40379f7b146" instanceOccurrenceNum="0" instanceOccurrenceMax="0">
                <ShortMessage>Boxing/unboxing to parse a primitive</ShortMessage>
                <LongMessage>Boxing/unboxing to parse a primitive Test.main(String[])</LongMessage>
                <Class classname="Test" primary="true">
                    <SourceLine classname="Test" start="2" end="9" sourcefile="Test.java" sourcepath="Test.java">
                        <Message>At Test.java:[lines 2-9]</Message>
                    </SourceLine>
                    <Message>In class Test</Message>
                </Class>
                <Method classname="Test" name="main" signature="([Ljava/lang/String;)V" isStatic="true" primary="true">
                    <SourceLine classname="Test" start="6" end="9" startBytecode="0" endBytecode="95" sourcefile="Test.java" sourcepath="Test.java"/>
                    <Message>In method Test.main(String[])</Message>
                </Method>
                <Method classname="java.lang.Integer" name="intValue" signature="()I" isStatic="false" role="METHOD_CALLED">
                    <SourceLine classname="java.lang.Integer" start="701" end="701" startBytecode="0" endBytecode="28" sourcefile="Integer.java" sourcepath="java/lang/Integer.java"/>
                    <Message>Called method Integer.intValue()</Message>
                </Method>
                <Method classname="java.lang.Integer" name="parseInt" signature="(Ljava/lang/String;)I" isStatic="true" role="SHOULD_CALL">
                    <Message>Should call Integer.parseInt(String) instead</Message>
                </Method>
                <SourceLine classname="Test" primary="true" start="7" end="7" startBytecode="14" endBytecode="14" sourcefile="Test.java" sourcepath="Test.java">
                    <Message>At Test.java:[line 7]</Message>
                </SourceLine>
            </BugInstance>
            <BugCategory category="PERFORMANCE">
                <Description>Performance</Description>
            </BugCategory>
            <BugPattern type="DM_BOXED_PRIMITIVE_FOR_PARSING" abbrev="Bx" category="PERFORMANCE">
                <ShortDescription>Boxing/unboxing to parse a primitive</ShortDescription>
                <Details><![CDATA[

  <p>A boxed primitive is created from a String, just to extract the unboxed primitive value.
  It is more efficient to just call the static parseXXX method.</p>

    ]]></Details>
            </BugPattern>
            <BugCode abbrev="Bx">
                <Description>Questionable Boxing of primitive value</Description>
            </BugCode>
            <Errors errors="0" missingClasses="0"></Errors>
            <FindBugsSummary timestamp="Fri, 15 Aug 2014 17:53:50 +0400" total_classes="1" referenced_classes="12" total_bugs="1" total_size="11" num_packages="1" java_version="1.7.0_45" vm_version="24.45-b08" cpu_seconds="5.27" clock_seconds="2.19" peak_mbytes="427.05" alloc_mbytes="910.50" gc_seconds="0.00" priority_1="1">
                <FileStats path="Test.java" bugCount="1" size="11" bugHash="19a731eca7158a3fd98e0376651e0fc6"/>
                <PackageStats package="" total_bugs="1" total_types="1" total_size="11" priority_1="1">
                    <ClassStats class="Test" sourceFile="Test.java" interface="false" size="11" bugs="1" priority_1="1"/>
                </PackageStats>
                <FindBugsProfile>
                    <ClassProfile name="edu.umd.cs.findbugs.classfile.engine.ClassInfoAnalysisEngine" totalMilliseconds="382" invocations="318" avgMicrosecondsPerInvocation="1202" maxMicrosecondsPerInvocation="30504" standardDeviationMircosecondsPerInvocation="2986"/>
                    <ClassProfile name="edu.umd.cs.findbugs.detect.FieldItemSummary" totalMilliseconds="146" invocations="12" avgMicrosecondsPerInvocation="12174" maxMicrosecondsPerInvocation="31997" standardDeviationMircosecondsPerInvocation="10567"/>
                    <ClassProfile name="edu.umd.cs.findbugs.OpcodeStack$JumpInfoFactory" totalMilliseconds="112" invocations="55" avgMicrosecondsPerInvocation="2052" maxMicrosecondsPerInvocation="8926" standardDeviationMircosecondsPerInvocation="1824"/>
                    <ClassProfile name="edu.umd.cs.findbugs.classfile.engine.bcel.MethodGenFactory" totalMilliseconds="86" invocations="3" avgMicrosecondsPerInvocation="28970" maxMicrosecondsPerInvocation="85322" standardDeviationMircosecondsPerInvocation="39849"/>
                    <ClassProfile name="edu.umd.cs.findbugs.util.TopologicalSort" totalMilliseconds="86" invocations="285" avgMicrosecondsPerInvocation="303" maxMicrosecondsPerInvocation="7601" standardDeviationMircosecondsPerInvocation="715"/>
                    <ClassProfile name="edu.umd.cs.findbugs.detect.NoteDirectlyRelevantTypeQualifiers" totalMilliseconds="69" invocations="12" avgMicrosecondsPerInvocation="5756" maxMicrosecondsPerInvocation="20146" standardDeviationMircosecondsPerInvocation="6071"/>
                    <ClassProfile name="edu.umd.cs.findbugs.classfile.engine.bcel.JavaClassAnalysisEngine" totalMilliseconds="58" invocations="16" avgMicrosecondsPerInvocation="3679" maxMicrosecondsPerInvocation="25791" standardDeviationMircosecondsPerInvocation="6792"/>
                    <ClassProfile name="edu.umd.cs.findbugs.detect.FunctionsThatMightBeMistakenForProcedures" totalMilliseconds="53" invocations="12" avgMicrosecondsPerInvocation="4421" maxMicrosecondsPerInvocation="13926" standardDeviationMircosecondsPerInvocation="4986"/>
                    <ClassProfile name="edu.umd.cs.findbugs.detect.BuildObligationPolicyDatabase" totalMilliseconds="46" invocations="12" avgMicrosecondsPerInvocation="3912" maxMicrosecondsPerInvocation="13289" standardDeviationMircosecondsPerInvocation="3635"/>
                    <ClassProfile name="edu.umd.cs.findbugs.detect.OverridingEqualsNotSymmetrical" totalMilliseconds="37" invocations="12" avgMicrosecondsPerInvocation="3139" maxMicrosecondsPerInvocation="11585" standardDeviationMircosecondsPerInvocation="3215"/>
                    <ClassProfile name="edu.umd.cs.findbugs.classfile.engine.ClassDataAnalysisEngine" totalMilliseconds="31" invocations="319" avgMicrosecondsPerInvocation="100" maxMicrosecondsPerInvocation="1023" standardDeviationMircosecondsPerInvocation="152"/>
                    <ClassProfile name="edu.umd.cs.findbugs.detect.CalledMethods" totalMilliseconds="29" invocations="12" avgMicrosecondsPerInvocation="2440" maxMicrosecondsPerInvocation="7034" standardDeviationMircosecondsPerInvocation="2423"/>
                </FindBugsProfile>
            </FindBugsSummary>
            <ClassFeatures></ClassFeatures>
            <History></History>
        </BugCollection>
}
