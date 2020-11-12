/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.intentions.control;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.intentions.base.Intention;
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate;
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.utils.BoolUtils;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrConditionalExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;

public class FlipConditionalIntention extends Intention {


  @Override
  @NotNull
  public PsiElementPredicate getElementPredicate() {
    return new ConditionalPredicate();
  }

  @Override
  public void processIntention(@NotNull PsiElement element, Project project, Editor editor)
      throws IncorrectOperationException {
    final GrConditionalExpression exp =
        (GrConditionalExpression) element;

    final GrExpression condition = exp.getCondition();
    final GrExpression elseExpression = exp.getElseBranch();
    final GrExpression thenExpression = exp.getThenBranch();
    assert elseExpression != null;
    assert thenExpression != null;
    final String newExpression =
        BoolUtils.getNegatedExpressionText(condition) + '?' +
            elseExpression.getText() +
            ':' +
            thenExpression.getText();
    PsiImplUtil.replaceExpression(newExpression, exp);
  }

}
