/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.datetime.calop;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.util.SimpleNumberCoercerFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class CalendarWithTimeForgeOp implements CalendarOp {
    public final static String METHOD_ACTIONSETHMSMCALENDAR = "actionSetHMSMCalendar";

    private ExprEvaluator hour;
    private ExprEvaluator min;
    private ExprEvaluator sec;
    private ExprEvaluator msec;

    public CalendarWithTimeForgeOp(ExprEvaluator hour, ExprEvaluator min, ExprEvaluator sec, ExprEvaluator msec) {
        this.hour = hour;
        this.min = min;
        this.sec = sec;
        this.msec = msec;
    }

    public void evaluate(Calendar cal, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
        Integer hourNum = CalendarWithDateForgeOp.getInt(hour, eventsPerStream, isNewData, context);
        Integer minNum = CalendarWithDateForgeOp.getInt(min, eventsPerStream, isNewData, context);
        Integer secNum = CalendarWithDateForgeOp.getInt(sec, eventsPerStream, isNewData, context);
        Integer msecNum = CalendarWithDateForgeOp.getInt(msec, eventsPerStream, isNewData, context);
        actionSetHMSMCalendar(cal, hourNum, minNum, secNum, msecNum);
    }

    public static CodegenExpression codegenCalendar(CalendarWithTimeForge forge, CodegenExpression cal, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethod methodNode = codegenMethodScope.makeChild(EPTypePremade.VOID.getEPType(), CalendarWithTimeForgeOp.class, codegenClassScope).addParam(EPTypePremade.CALENDAR.getEPType(), "cal");

        CodegenBlock block = methodNode.getBlock();
        codegenDeclareInts(block, forge, methodNode, exprSymbol, codegenClassScope);
        block.staticMethod(CalendarWithTimeForgeOp.class, METHOD_ACTIONSETHMSMCALENDAR, ref("cal"), ref("hour"), ref("minute"), ref("second"), ref("msec"));
        return localMethod(methodNode, cal);
    }

    public LocalDateTime evaluate(LocalDateTime ldt, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
        Integer hourNum = CalendarWithDateForgeOp.getInt(hour, eventsPerStream, isNewData, context);
        Integer minNum = CalendarWithDateForgeOp.getInt(min, eventsPerStream, isNewData, context);
        Integer secNum = CalendarWithDateForgeOp.getInt(sec, eventsPerStream, isNewData, context);
        Integer msecNum = CalendarWithDateForgeOp.getInt(msec, eventsPerStream, isNewData, context);
        return actionSetHMSMLocalDateTime(ldt, hourNum, minNum, secNum, msecNum);
    }

    public static CodegenExpression codegenLDT(CalendarWithTimeForge forge, CodegenExpression ldt, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethod methodNode = codegenMethodScope.makeChild(EPTypePremade.LOCALDATETIME.getEPType(), CalendarWithTimeForgeOp.class, codegenClassScope).addParam(EPTypePremade.LOCALDATETIME.getEPType(), "ldt");


        CodegenBlock block = methodNode.getBlock();
        codegenDeclareInts(block, forge, methodNode, exprSymbol, codegenClassScope);
        block.methodReturn(staticMethod(CalendarWithTimeForgeOp.class, "actionSetHMSMLocalDateTime", ref("ldt"), ref("hour"), ref("minute"), ref("second"), ref("msec")));
        return localMethod(methodNode, ldt);
    }

    public ZonedDateTime evaluate(ZonedDateTime zdt, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
        Integer hourNum = CalendarWithDateForgeOp.getInt(hour, eventsPerStream, isNewData, context);
        Integer minNum = CalendarWithDateForgeOp.getInt(min, eventsPerStream, isNewData, context);
        Integer secNum = CalendarWithDateForgeOp.getInt(sec, eventsPerStream, isNewData, context);
        Integer msecNum = CalendarWithDateForgeOp.getInt(msec, eventsPerStream, isNewData, context);
        return actionSetHMSMZonedDateTime(zdt, hourNum, minNum, secNum, msecNum);
    }

    public static CodegenExpression codegenZDT(CalendarWithTimeForge forge, CodegenExpression zdt, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethod methodNode = codegenMethodScope.makeChild(EPTypePremade.ZONEDDATETIME.getEPType(), CalendarWithTimeForgeOp.class, codegenClassScope).addParam(EPTypePremade.ZONEDDATETIME.getEPType(), "zdt");


        CodegenBlock block = methodNode.getBlock();
        codegenDeclareInts(block, forge, methodNode, exprSymbol, codegenClassScope);
        block.methodReturn(staticMethod(CalendarWithTimeForgeOp.class, "actionSetHMSMZonedDateTime", ref("zdt"), ref("hour"), ref("minute"), ref("second"), ref("msec")));
        return localMethod(methodNode, zdt);
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param cal    cal
     * @param hour   hour
     * @param minute min
     * @param second sec
     * @param msec   msec
     */
    public static void actionSetHMSMCalendar(Calendar cal, Integer hour, Integer minute, Integer second, Integer msec) {
        if (hour != null) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            cal.set(Calendar.MINUTE, minute);
        }
        if (second != null) {
            cal.set(Calendar.SECOND, second);
        }
        if (msec != null) {
            cal.set(Calendar.MILLISECOND, msec);
        }
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param ldt    ldt
     * @param hour   hour
     * @param minute min
     * @param second sec
     * @param msec   msec
     * @return ldt
     */
    public static LocalDateTime actionSetHMSMLocalDateTime(LocalDateTime ldt, Integer hour, Integer minute, Integer second, Integer msec) {
        if (hour != null) {
            ldt = ldt.with(ChronoField.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            ldt = ldt.with(ChronoField.MINUTE_OF_HOUR, minute);
        }
        if (second != null) {
            ldt = ldt.with(ChronoField.SECOND_OF_MINUTE, second);
        }
        if (msec != null) {
            ldt = ldt.with(ChronoField.MILLI_OF_SECOND, msec);
        }
        return ldt;
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param zdt    zdt
     * @param hour   hour
     * @param minute min
     * @param second sec
     * @param msec   msec
     * @return ldt
     */
    public static ZonedDateTime actionSetHMSMZonedDateTime(ZonedDateTime zdt, Integer hour, Integer minute, Integer second, Integer msec) {
        if (hour != null) {
            zdt = zdt.with(ChronoField.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            zdt = zdt.with(ChronoField.MINUTE_OF_HOUR, minute);
        }
        if (second != null) {
            zdt = zdt.with(ChronoField.SECOND_OF_MINUTE, second);
        }
        if (msec != null) {
            zdt = zdt.with(ChronoField.MILLI_OF_SECOND, msec);
        }
        return zdt;
    }

    private static void codegenDeclareInts(CodegenBlock block, CalendarWithTimeForge forge, CodegenMethod methodNode, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        EPTypeClass hourType = (EPTypeClass) forge.hour.getEvaluationType();
        EPTypeClass minType = (EPTypeClass) forge.min.getEvaluationType();
        EPTypeClass secType = (EPTypeClass) forge.sec.getEvaluationType();
        EPTypeClass msecType = (EPTypeClass) forge.msec.getEvaluationType();
        block.declareVar(EPTypePremade.INTEGERBOXED.getEPType(), "hour", SimpleNumberCoercerFactory.SimpleNumberCoercerInt.coerceCodegenMayNull(forge.hour.evaluateCodegen(hourType, methodNode, exprSymbol, codegenClassScope), hourType, methodNode, codegenClassScope))
                .declareVar(EPTypePremade.INTEGERBOXED.getEPType(), "minute", SimpleNumberCoercerFactory.SimpleNumberCoercerInt.coerceCodegenMayNull(forge.min.evaluateCodegen(minType, methodNode, exprSymbol, codegenClassScope), minType, methodNode, codegenClassScope))
                .declareVar(EPTypePremade.INTEGERBOXED.getEPType(), "second", SimpleNumberCoercerFactory.SimpleNumberCoercerInt.coerceCodegenMayNull(forge.sec.evaluateCodegen(secType, methodNode, exprSymbol, codegenClassScope), secType, methodNode, codegenClassScope))
                .declareVar(EPTypePremade.INTEGERBOXED.getEPType(), "msec", SimpleNumberCoercerFactory.SimpleNumberCoercerInt.coerceCodegenMayNull(forge.msec.evaluateCodegen(msecType, methodNode, exprSymbol, codegenClassScope), msecType, methodNode, codegenClassScope));
    }
}
