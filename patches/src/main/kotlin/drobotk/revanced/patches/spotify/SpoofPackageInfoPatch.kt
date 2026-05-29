package drobotk.revanced.patches.spotify

import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.forEachInstructionAsSequence
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

@Suppress("unused")
val spoofPackageInfoPatch = bytecodePatch(
    name = "Spoof package info",
    description = "Spoofs the package info of the app to fix various functions of the app.",
) {
    compatibleWith("com.spotify.music")

    apply {
        getPackageInfoMethodMatch.method.apply {
            // region Spoof signature.

            val concatSignaturesIndex = indexOfFirstInstructionReversedOrThrow(
                getPackageInfoMethodMatch[0],
                Opcode.MOVE_RESULT_OBJECT,
            )

            val signatureRegister = getInstruction<OneRegisterInstruction>(concatSignaturesIndex).registerA
            val expectedSignature = "d6a6dced4a85f24204bf9505ccc1fce114cadb32"

            replaceInstruction(concatSignaturesIndex, "const-string v$signatureRegister, \"$expectedSignature\"")

            // endregion

//            findInstructionIndicesReversedOrThrow {
//                methodReference?.name.let {
//                    it == "getInstallerPackageName" || it == "getInstallingPackageName"
//                }
//            }.forEach { index ->
//                val returnObjectIndex = index + 1
//
//                val installerPackageNameRegister = getInstruction<OneRegisterInstruction>(
//                    returnObjectIndex
//                ).registerA
//
//                addInstruction(
//                    returnObjectIndex + 1,
//                    "const-string v$installerPackageNameRegister, \"$expectedInstallerName\""
//                )
//            }
        }

        // region Spoof installer name.

        val expectedInstallerName = "com.android.vending"

        forEachInstructionAsSequence(
            match = { _, _, instruction, instructionIndex ->
                if (instruction !is ReferenceInstruction) return@forEachInstructionAsSequence null

                val reference = instruction.methodReference ?: return@forEachInstructionAsSequence null

                val match = MethodCall.entries.firstOrNull { search ->
                    MethodUtil.methodSignaturesMatch(reference, search.reference)
                } ?: return@forEachInstructionAsSequence null

                instructionIndex
            },
            transform = { method, index ->
                val nextInstr = method.getInstruction<Instruction>(index + 1)

                if (nextInstr.opcode.name == "move-result-object") {
                    val register = (nextInstr as OneRegisterInstruction).registerA
                    method.replaceInstruction(index, "const-string v$register, \"$expectedInstallerName\"")
                    method.replaceInstruction(index + 1, "nop")
                } else {
                    method.replaceInstruction(index, "nop")
                }
            }
        )

        // endregion
    }
}

private enum class MethodCall(
    val reference: MethodReference,
) {
    GetInstallerPackageName(
        ImmutableMethodReference(
            "Landroid/content/pm/PackageManager;",
            "getInstallerPackageName",
            listOf("Ljava/lang/String;"),
            "Ljava/lang/String;",
        )
    ),
    GetInstallingPackageName(
        ImmutableMethodReference(
            "Landroid/content/pm/InstallSourceInfo;",
            "getInstallingPackageName",
            emptyList(),
            "Ljava/lang/String;",
        )
    ),
    GetInitiatingPackageName(
        ImmutableMethodReference(
            "Landroid/content/pm/InstallSourceInfo;",
            "getInitiatingPackageName",
            emptyList(),
            "Ljava/lang/String;",
        )
    )
}