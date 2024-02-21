package io.github.surpsg.deltacoverage.lib.jvm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

internal fun readClassFileName(classBytes: ByteArray): String? {
    val customClassVisitor = CustomClassVisitor()
    ClassReader(classBytes).accept(customClassVisitor, ClassReader.SKIP_CODE)
    return customClassVisitor.sourceName
}

private class CustomClassVisitor : ClassVisitor(Opcodes.ASM9) {

    var sourceName: String? = null

    override fun visitSource(source: String?, debug: String?) {
        super.visitSource(source, debug)
        sourceName = source
    }
}
