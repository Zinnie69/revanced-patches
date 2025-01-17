package app.revanced.patches.youtube.layout

import app.revanced.patcher.data.implementation.BytecodeData
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
import app.revanced.patcher.smali.toInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.17.34")
    )
)

class HideReelsPatch : BytecodePatch(
    PatchMetadata(
        "hide-reels",
        "Hide reels patch",
        "Hide reels on the page.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "hide-reels-signature",
                MethodMetadata("Ljvy", "<init>"), // unknown
                PatternScanMethod.Fuzzy(3), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Signature for the method required to be patched.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            listOf(
                "L",
                "L",
                "L",
                "L",
                "L",
                "L",
                "L",
                "L",
                "L",
                "L",
                "L",
                "[B",
                "[B",
                "[B",
                "[B",
                "[B",
                "[B"
            ),
            listOf(
                Opcode.MOVE_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.INVOKE_DIRECT,
                Opcode.MOVE_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.MOVE_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.IPUT_OBJECT,
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.IPUT_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.IPUT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST,
                Opcode.CONST_4,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IPUT_OBJECT
            )
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = signatures.first().result!!
        val implementation = result.method.implementation!!

        // HideReel will hide the reel view before it is being used,
        // so we pass the view to the HideReel method
        implementation.addInstruction(
            result.scanData.endIndex,
            "invoke-static { v2 }, Lfi/razerman/youtube/XAdRemover;->HideReel(Landroid/view/View;)V".toInstruction()
        )

        return PatchResultSuccess()
    }
}