package app.revanced.patches.youtube.layout

import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.17.34")
    )
)

class OldQualityLayoutPatch : BytecodePatch(
    PatchMetadata(
        "old-quality-layout",
        "Old Quality Layout Patch",
        "Enable the original quality flyout menu",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "old-quality-parent-method-signature",
                MethodMetadata("Libh", "<init>"), // unknown
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature to find a parent method required by the Old Quality Layout patch.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            listOf("L", "L", "L", "L", "L", "L", "L"),
            listOf(
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET_BOOLEAN,
                Opcode.CONST_4,
                Opcode.CONST_4,
                Opcode.CONST_4,
            )
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        var result = signatures.first().result!!

        result = result.findParentMethod(
            MethodSignature(
                MethodSignatureMetadata(
                    "old-quality-method-signature",
                    MethodMetadata("Libh", null), // unknown
                    PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                    compatiblePackages,
                    "Signature to find the method required by the Old Quality Layout patch",
                    "0.0.1"
                ),
                "L",
                AccessFlags.FINAL or AccessFlags.PRIVATE,
                listOf("Z"),
                listOf(
                    Opcode.CONST_4,
                    Opcode.INVOKE_VIRTUAL,
                    Opcode.IGET_OBJECT,
                    Opcode.IGET_OBJECT,
                    Opcode.INVOKE_VIRTUAL,
                    Opcode.IGET_OBJECT,
                    Opcode.GOTO,
                    Opcode.IGET_OBJECT,
                )
            )
        ) ?: return PatchResultError("Method old-quality-patch-method has not been found")

        val implementation = result.method.implementation!!

        // if useOldStyleQualitySettings == true, jump over all instructions
        val jmpInstruction =
            BuilderInstruction21t(
                Opcode.IF_NEZ,
                0,
                implementation.instructions[result.scanData.endIndex].location.labels.first()
            )
        implementation.addInstruction(5, jmpInstruction)
        implementation.addInstructions(
            0,
            """
                invoke-static { }, Lfi/razerman/youtube/XGlobals;->useOldStyleQualitySettings()Z
                move-result v0
            """.trimIndent().toInstructions()
        )

        return PatchResultSuccess()
    }
}