package io.github.surpsg.deltacoverage.report.jacoco.filters

import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.diff.parse.ClassFile
import org.jacoco.core.internal.analysis.filter.IFilter
import org.jacoco.core.internal.analysis.filter.IFilterContext
import org.jacoco.core.internal.analysis.filter.IFilterOutput
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import org.slf4j.LoggerFactory

internal class ModifiedLinesFilter(private val codeUpdateInfo: CodeUpdateInfo) : IFilter {

    override fun filter(
        methodNode: MethodNode,
        context: IFilterContext,
        output: IFilterOutput
    ) {
        val classModifications = codeUpdateInfo.getClassModifications(
            ClassFile(
                context.sourceFileName,
                context.className
            )
        )
        val groupedModifiedLines = collectLineNodes(methodNode.instructions).groupBy {
            classModifications.isLineModified(it.lineNode.line)
        }

        val modifiedLines: List<LineNode> = groupedModifiedLines[true] ?: emptyList()
        logModifiedLines(context.className, methodNode.name, modifiedLines)

        if (modifiedLines.isEmpty()) {
            output.ignore(methodNode.instructions.first, methodNode.instructions.last)
        } else {
            groupedModifiedLines[false]?.forEach {
                output.ignore(it.lineNode.previous, it.lineNodeLastInstruction)
            }
        }
    }

    private fun logModifiedLines(
        className: String,
        methodName: String,
        modifiedLines: List<LineNode>
    ) {
        if (log.isDebugEnabled) {
            val linesNumbers: List<Int> = modifiedLines.map { it.lineNode.line }
            log.debug("Matched modified lines in {}#{}: {}", className, methodName, linesNumbers)
        }
    }

    private fun collectLineNodes(instructionNodes: InsnList): Sequence<LineNode> {
        val lineNodes = ArrayList<LineNode>()

        val iterator = instructionNodes.iterator()
        val nextLineNode = getNextLineNode(iterator) ?: return emptySequence()

        var currentNode = LineNode(nextLineNode)
        while (iterator.hasNext()) {
            val instructionNode = iterator.next()
            if (instructionNode is LabelNode && instructionNode.next is LineNumberNode) {
                lineNodes.add(currentNode)
                currentNode = LineNode(instructionNode.next as LineNumberNode)
            } else {
                currentNode.lineNodeLastInstruction = instructionNode
            }
        }
        lineNodes.add(currentNode)
        return lineNodes.asSequence()
    }

    private fun getNextLineNode(instructionNodes: ListIterator<AbstractInsnNode>): LineNumberNode? {
        while (instructionNodes.hasNext()) {
            val node = instructionNodes.next()
            if (node is LineNumberNode) {
                return node
            }
        }
        return null
    }

    private class LineNode(
        val lineNode: LineNumberNode,
        var lineNodeLastInstruction: AbstractInsnNode = lineNode
    )

    internal companion object {
        var log = LoggerFactory.getLogger(ModifiedLinesFilter::class.java)
    }
}
