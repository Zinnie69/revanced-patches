package app.revanced.patches.youtube.layout

import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.14.35", "17.17.34")
    )
)

class MinimizedPlaybackPatch : BytecodePatch(
    PatchMetadata(
        "minimized-playback",
        "Minimized Playback Patch",
        "Enable minimized and background playback.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "minimized-playback-manager",
                MethodMetadata("Lype", "j"), // unknown
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature for the method required to be patched.",
                "0.0.1"
            ),
            "Z",
            AccessFlags.PUBLIC or AccessFlags.STATIC,
            listOf("L"),
            listOf(
                Opcode.CONST_4,
                Opcode.IF_EQZ,
                Opcode.IGET,
                Opcode.AND_INT_LIT16,
                Opcode.IF_EQZ,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET,
                Opcode.CONST,
                Opcode.IF_NE,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IGET,
                Opcode.IF_NE,
                Opcode.IGET_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.GOTO,
                Opcode.SGET_OBJECT,
                Opcode.GOTO,
                Opcode.CONST_4,
                Opcode.IF_EQZ,
                Opcode.IGET_BOOLEAN,
                Opcode.IF_EQZ
            )
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        signatures.first().result!!.method.implementation!!.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
                """.trimIndent().toInstructions()
        )
        return PatchResultSuccess()
    }
}