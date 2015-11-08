package info.izumin.android.droidux.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.SetMultimap;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import info.izumin.android.droidux.annotation.CombinedReducer;
import info.izumin.android.droidux.annotation.Reducer;
import info.izumin.android.droidux.processor.element.CombinedStoreClassElement;
import info.izumin.android.droidux.processor.element.StoreClassElement;
import info.izumin.android.droidux.processor.model.CombinedReducerModel;
import info.izumin.android.droidux.processor.model.ReducerModel;

import static info.izumin.android.droidux.processor.util.AnnotationUtils.getClassesFromAnnotation;

@AutoService(Processor.class)
public class DroiduxProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        return new ArrayList<ProcessingStep>() {{
            add(reducerProcessingStep);
            add(combinedReducerProcessingStep);
        }};
    }

    private Filer getFiler() {
        return super.processingEnv.getFiler();
    }

    private Elements getElements() {
        return super.processingEnv.getElementUtils();
    }

    private final ProcessingStep reducerProcessingStep = new ProcessingStep() {
        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return new HashSet<Class<? extends Annotation>>() {{ add(Reducer.class); }};
        }

        @Override
        public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
            for (Element element : elementsByAnnotation.get(Reducer.class)) {
                try {
                    new StoreClassElement(new ReducerModel((TypeElement) element)).createJavaFile().writeTo(getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new HashSet<>();
        }
    };

    private final ProcessingStep combinedReducerProcessingStep = new ProcessingStep() {
        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return new HashSet<Class<? extends Annotation>>() {{ add(CombinedReducer.class); }};
        }

        @Override
        public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
            for (Element element : elementsByAnnotation.get(CombinedReducer.class)) {
                List<TypeElement> reducers = new ArrayList<>();
                for (ClassName reducerClass : getClassesFromAnnotation(element, CombinedReducer.class, "value")) {
                    reducers.add(getElements().getTypeElement(reducerClass.toString()));
                }
                try {
                    new CombinedStoreClassElement(new CombinedReducerModel((TypeElement) element, reducers))
                            .createJavaFile().writeTo(getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new HashSet<>();
        }
    };
}
