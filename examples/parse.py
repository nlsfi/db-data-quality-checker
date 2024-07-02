import yaml
import pathlib
import jinja2

CONFIG = {
    "id_column": "fid",
    "geometry_column": "geom",
}


def str_presenter(dumper, data):
    if data.count('\n') > 0:
        data = "\n".join([line.rstrip() for line in data.splitlines() if line.rstrip() != ''])
        return dumper.represent_scalar('tag:yaml.org,2002:str', data, style='|')
    return dumper.represent_scalar('tag:yaml.org,2002:str', data)

yaml.add_representer(str, str_presenter)

templates = yaml.load((pathlib.Path(__file__).parent / 'rule-templates.yml').read_text(encoding='utf-8'), Loader=yaml.FullLoader)
config = yaml.load((pathlib.Path(__file__).parent / 'rule-config.yml').read_text(encoding='utf-8'), Loader=yaml.FullLoader)

env = jinja2.Environment(loader=jinja2.BaseLoader())

rules = []

for source_table, rule_configs in config.items():
    for rule_key, rule_config_list in rule_configs.items():
        rule_template = templates[rule_key]
        for rule_config in rule_config_list:
            rules.append(
                {"rule_key": rule_key} |
                rule_template | {
                    "sql": env.from_string(rule_template["sql"]).render(**(CONFIG | {"source": source_table} | rule_template | rule_config)),
                    "descriptions": [
                        (d | {"description": env.from_string(d["description"]).render(**rule_config)}) for d in rule_template["descriptions"]
                    ]
                } | rule_config
            )

(pathlib.Path(__file__).parent / 'quality_rules.yml').write_text(yaml.dump({'quality_rules': rules}), encoding='utf-8')
