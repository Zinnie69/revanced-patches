package app.revanced.patches.youtube.ad

import app.revanced.extensions.injectHideCall
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.data.implementation.toMethodWalker
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction11x

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.03.38", "17.14.35", "17.17.34")
    )
)

private val patchMetadata = PatchMetadata(
    "home-promo-ads",
    "Home Promo Ads Patch",
    "Patch to remove promoted ads in YouTube",
    compatiblePackages,
    "0.0.1"
)

private val signatureDescription = "Required signature for ${patchMetadata.name}. Discovered in version 17.03.38."

class HomePromoPatch : BytecodePatch(
    patchMetadata,
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "promoted-discovery-app-parent-method",
                MethodMetadata(
                    "Ljre;",
                    "lP",
                ),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                signatureDescription,
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.BRIDGE or AccessFlags.SYNTHETIC,
            listOf("L", "L"),
            listOf(
                Opcode.INVOKE_DIRECT,
                Opcode.IGET_BOOLEAN,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.NEW_ARRAY,
                Opcode.IPUT_OBJECT,
                Opcode.CONST_4,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.IF_GE,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.APUT_OBJECT,
                Opcode.ADD_INT_LIT8,
                Opcode.GOTO
            )
        ),
        MethodSignature(
            MethodSignatureMetadata(
                "promoted-discovery-action-parent-method",
                MethodMetadata(
                    "Ljqv;",
                    "lP",
                ),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                signatureDescription,
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.BRIDGE or AccessFlags.SYNTHETIC,
            listOf("L", "L"),
            listOf(
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.MOVE_OBJECT_FROM16,
                Opcode.CHECK_CAST,
                Opcode.INVOKE_VIRTUAL_RANGE,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IGET_OBJECT,
                Opcode.IGET_BOOLEAN,
                Opcode.CONST_4,
                Opcode.XOR_INT_2ADDR,
                Opcode.IGET_BOOLEAN,
                Opcode.INVOKE_DIRECT,
                Opcode.IGET_BOOLEAN,
                Opcode.INVOKE_VIRTUAL,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.SGET_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.CONST_4,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.IGET_OBJECT
            )
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        for (signature in signatures) {
            val result = signature.result!!

            val methodMetadata = MethodMetadata(signature.metadata.methodMetadata!!.definingClass, "d")
            val requiredMethod = result.findParentMethod(
                MethodSignature(
                    MethodSignatureMetadata(
                        "promoted-discovery-action-method",
                        methodMetadata,
                        PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                        compatiblePackages,
                        signatureDescription,
                        "0.0.1"
                    ),
                    "V",
                    AccessFlags.PRIVATE or AccessFlags.FINAL,
                    listOf("Z", "Z"),
                    null
                )
            )
                ?: return PatchResultError("Required parent method ${methodMetadata.name} could not be found in ${methodMetadata.definingClass}")

            val toBePatchedInvokeOffset =
                requiredMethod.immutableMethod.implementation!!.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_DIRECT }
            val toBePatchedMethod = data
                .toMethodWalker(requiredMethod.immutableMethod)
                .walk(toBePatchedInvokeOffset, true)
                .getMethod() as MutableMethod

            val implementation = toBePatchedMethod.implementation!!
            val invokeVirtualOffset = implementation.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_VIRTUAL }

            val moveResultInstruction = implementation.instructions[invokeVirtualOffset + 1]
            if (moveResultInstruction.opcode != Opcode.MOVE_RESULT_OBJECT)
                return PatchResultError("The toBePatchedInvokeOffset offset was wrong in ${metadata.name}")

            val register = (moveResultInstruction as Instruction11x).registerA
            implementation.injectHideCall(invokeVirtualOffset + 2, register)
        }

        return PatchResultSuccess()
    }
}