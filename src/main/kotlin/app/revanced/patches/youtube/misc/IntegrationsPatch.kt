package app.revanced.patches.youtube.misc

import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodImplementation

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.03.38", "17.14.35", "17.17.34")
    )
)

class IntegrationsPatch : BytecodePatch(
    PatchMetadata(
        "integrations",
        "Inject Integrations Patch",
        "Applies mandatory patches to implement the ReVanced integrations into the application.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "integrations-patch",
                MethodMetadata("Lacnx", "onCreate"),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Inject the integrations into the application with the method of this signature",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC.value,
            listOf(),
            listOf(
                Opcode.SGET_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IGET_OBJECT,
                Opcode.CONST_STRING,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.MOVE_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.CONST_4,
                Opcode.CONST_STRING,
                Opcode.INVOKE_INTERFACE_RANGE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.SPUT_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.INVOKE_VIRTUAL,
                Opcode.INVOKE_SUPER,
                Opcode.INVOKE_VIRTUAL
            )
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = signatures.first().result!!

        val implementation = result.method.implementation!!
        val count = implementation.registerCount - 1

        implementation.addInstructions(
            result.scanData.endIndex + 1,
            """
                  invoke-static {v$count}, Lpl/jakubweg/StringRef;->setContext(Landroid/content/Context;)V
                  sput-object v$count, Lapp/revanced/integrations/Globals;->context:Landroid/content/Context;
            """.trimIndent().toInstructions()
        )

        val classDef = result.definingClassProxy.resolve()
        classDef.methods.add(
            ImmutableMethod(
                classDef.type,
                "getAppContext",
                null,
                "Landroid/content/Context;",
                AccessFlags.PUBLIC or AccessFlags.STATIC,
                null,
                null,
                ImmutableMethodImplementation(
                    1,
                    """
                        invoke-static { }, Lapp/revanced/integrations/Globals;->getAppContext()Landroid/content/Context;
                        move-result-object v0
                        return-object v0
                    """.trimIndent().toInstructions(),
                    null,
                    null
                )
            ).toMutable()
        )
        return PatchResultSuccess()
    }
}