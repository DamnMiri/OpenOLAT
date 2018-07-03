/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.ui;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityManager;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.ui.ParticipationDataModel.ParticipationCols;
import org.olat.modules.quality.ui.wizard.AddCourseUser_1_ChooseCourseStep;
import org.olat.modules.quality.ui.wizard.AddCurriculumElementUser_1_ChooseCurriculumElementStep;
import org.olat.modules.quality.ui.wizard.AddUser_1_ChooseUserStep;
import org.olat.modules.quality.ui.wizard.CourseContext;
import org.olat.modules.quality.ui.wizard.CurriculumElementContext;
import org.olat.modules.quality.ui.wizard.IdentityContext;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationListController extends FormBasicController implements TooledController {

	private Link addUsersLink;
	private Link addCourseUsersLink;
	private Link addCurriculumElementUsersLink;
	private FormLink removeUsersLink;
	private ParticipationDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private final TooledStackedPanel stackPanel;
	private StepsMainRunController wizard;
	private CloseableModalController cmc;
	private ParticipationRemoveConfirmationController removeConformationCtrl;
	
	private final QualitySecurityCallback secCallback;
	private QualityDataCollection dataCollection;
	
	@Autowired
	private QualityManager qualityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;

	public ParticipationListController(UserRequest ureq, WindowControl windowControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollectionLight dataCollection) {
		super(ureq, windowControl, "participation_list");
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollection = qualityManager.loadDataCollectionByKey(dataCollection);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.firstname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.lastname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.role, new QualityContextRoleRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.repositoryEntryName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.curriculumElementName));
		
		ParticipationDataSource dataSource = new ParticipationDataSource(dataCollection);
		dataModel = new ParticipationDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "participations", dataModel, 25, true, getTranslator(), formLayout);
		if (secCallback.canRevomeParticipation(dataCollection)) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
		}
		
		if (secCallback.canRevomeParticipation(dataCollection)) {
			removeUsersLink = uifactory.addFormLink("participation.remove", formLayout, Link.BUTTON);
		}
	}

	@Override
	public void initTools() {
		addCourseUsersLink = LinkFactory.createToolLink("participation.user.add.course", translate("participation.user.add.course"), this);
		addCourseUsersLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_part_user_add_course");
		stackPanel.addTool(addCourseUsersLink, Align.right);
		
		addCurriculumElementUsersLink = LinkFactory.createToolLink("participation.user.add.curele", translate("participation.user.add.curele"), this);
		addCurriculumElementUsersLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_part_user_add_curele");
		stackPanel.addTool(addCurriculumElementUsersLink, Align.right);;
		
		addUsersLink = LinkFactory.createToolLink("participation.user.add", translate("participation.user.add"), this);
		addUsersLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_part_user_add");
		stackPanel.addTool(addUsersLink, Align.right);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			if (link == removeUsersLink) {
				List<QualityContextRef> contextRefs = getSelectedContextRefs();
				doConfirmRemove(ureq, contextRefs);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (addUsersLink == source) {
			doAddUsers(ureq);
		} else if (addCourseUsersLink == source) {
			doAddCourseUsers(ureq);
		} else if (addCurriculumElementUsersLink == source) {
			doAddCurriculumElementUsers(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (wizard == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					tableEl.reset(true, false, true);
				}
				cleanUp();
			}
		} else if (source == removeConformationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				List<QualityContextRef> contextRefs = removeConformationCtrl.getContextRefs();
				doRemove(contextRefs);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(removeConformationCtrl);
		removeAsListenerAndDispose(wizard);
		removeAsListenerAndDispose(cmc);
		removeConformationCtrl = null;
		wizard = null;
		cmc = null;
	}
	
	private List<QualityContextRef> getSelectedContextRefs() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.map(ParticipationRow::getContextRef)
				.collect(Collectors.toList());
	}

	private void doAddUsers(UserRequest ureq) {
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(), new AddUser_1_ChooseUserStep(ureq),
				addSelectedUsers(), null, translate("participation.user.add.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}
	
	private StepRunnerCallback addSelectedUsers() {
		return (uureq, wControl, runContext) -> {
			IdentityContext identityContext = (IdentityContext) runContext.get("context");
			Collection<Identity> identities = identityContext.getIdentities();
			List<EvaluationFormParticipation> participations = qualityManager.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityManager.createContextBuilder(dataCollection, participation).build();
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
	}
	
	private void doAddCourseUsers(UserRequest ureq) {
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(), new AddCourseUser_1_ChooseCourseStep(ureq),
				addSelectedCourseUsers(), null, translate("participation.user.course.add.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}
	
	private StepRunnerCallback addSelectedCourseUsers() {
		return (uureq, wControl, runContext) -> {
			CourseContext courseContext = (CourseContext) runContext.get("context");
			for (GroupRoles role: courseContext.getRoles()) {
				String roleName = role.name();
				for (RepositoryEntry repositoryEntry: courseContext.getRepositoryEntries()) {
					Collection<Identity> identities = repositoryService.getMembers(repositoryEntry, roleName);
					List<EvaluationFormParticipation> participations = qualityManager.addParticipations(dataCollection, identities);
					for (EvaluationFormParticipation participation: participations) {
						qualityManager.createContextBuilder(dataCollection, participation, repositoryEntry, role).build();
					}
				}
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
	}
	
	private void doAddCurriculumElementUsers(UserRequest ureq) {
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(),
				new AddCurriculumElementUser_1_ChooseCurriculumElementStep(ureq), addSelectedCurriculumElementUsers(),
				null, translate("participation.user.curele.add.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}
	
	private StepRunnerCallback addSelectedCurriculumElementUsers() {
		return (uureq, wControl, runContext) -> {
			CurriculumElementContext curriculumElementContext = (CurriculumElementContext) runContext.get("context");
			CurriculumElement curriculumElement = curriculumElementContext.getCurriculumElement();
			for (CurriculumRoles role: curriculumElementContext.getRoles()) {
				List<Identity> identities = curriculumService.getMembersIdentity(curriculumElement, role);
				List<EvaluationFormParticipation> participations = qualityManager.addParticipations(dataCollection, identities);
				for (EvaluationFormParticipation participation: participations) {
					qualityManager.createContextBuilder(dataCollection, participation, curriculumElement, role).build();
				}
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
	}

	private void doConfirmRemove(UserRequest ureq, List<QualityContextRef> contextRefs) {
		if (contextRefs.isEmpty()) {
			showWarning("participation.none.selected");
		} else {
			removeConformationCtrl = new ParticipationRemoveConfirmationController(ureq, getWindowControl(), contextRefs);
			listenTo(removeConformationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					removeConformationCtrl.getInitialComponent(), true, translate("participation.remove"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doRemove(List<QualityContextRef> contextRefs) {
		qualityManager.deleteContextsAndParticipations(contextRefs);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
